package flwr;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import flwr.proto.ClientMessage;
import flwr.proto.Parameters;
import flwr.Scalar;
import flwr.proto.ClientMessage.ParametersRes;
import flwr.proto.ClientMessage.FitRes;
import flwr.proto.ClientMessage.EvaluateRes;
import flwr.proto.ServerMessage.FitIns;
import flwr.proto.ServerMessage.EvaluateIns;

public class TFLiteClientWrapper implements Client {

    private TFLiteClient client;

    public TFLiteClientWrapper(TFLiteClient client) {
        this.client = client;
    }

    @Override
    public ClientMessage.ParametersRes getParameters() {
        List<ByteString> layers = paramsToByteStringList(client.getParameters());
        Parameters p = Parameters.newBuilder().addAllTensors(layers).setTensorType("ND").build();
        ClientMessage.ParametersRes res = ParametersRes.newBuilder().setParameters(p).build();
        return res;
    }

    @Override 
    public ClientMessage.FitRes fit(FitIns ins) {
        ByteBuffer[] newWeights = paramsFromByteStringList(ins.getParameters());
        Map<String, Scalar> config = wrapConfig(ins.getConfig());
        TFLiteClient.FitRes res = client.fit(newWeights, config);
        return fromTFLiteFitRes(res);
    }

    @Override
    public ClientMessage.EvaluateRes evaluate(EvaluateIns ins) {
        ByteBuffer[] newWeights = paramsFromByteStringList(ins.getParameters());
        Map<String, Scalar> config = wrapConfig(ins.getConfig());
        TFLiteClient.EvaluateRes res = client.evaluate(newWeights, config);
        return fromTFLiteEvaluateRes(res);
    }

    private static ClientMessage.FitRes fromTFLiteFitRes(TFLiteClient.FitRes fitRes) {
        List<ByteString> layers = paramsToByteStringList(fitRes.parameters);
        Parameters p = Parameters.newBuilder().addAllTensors(layers).setTensorType("ND").build();
        ClientMessage.FitRes.Builder builder = ClientMessage.FitRes.newBuilder()
            .setParameters(p)
            .setNumExamples(fitRes.num_examples);
        if (fitRes.metrics != null) {
            for (Map.Entry<String, Scalar> i : fitRes.metrics.entrySet()) {
                builder.putMetrics(i.getKey(), i.getValue().getProtoScalar());
            }
        }
        return builder.build();
    }

    private static ClientMessage.EvaluateRes fromTFLiteEvaluateRes(TFLiteClient.EvaluateRes evalRes) {
        ClientMessage.EvaluateRes.Builder builder = ClientMessage.EvaluateRes.newBuilder()
            .setNumExamples(evalRes.num_examples)
            .setLoss(evalRes.loss);
        if (evalRes.metrics != null) {
            for (Map.Entry<String, Scalar> i : evalRes.metrics.entrySet()) {
                builder.putMetrics(i.getKey(), i.getValue().getProtoScalar());
            }
        }
        return builder.build();
    }

    private static ByteBuffer[] paramsFromByteStringList(Parameters params) {
        List<ByteString> layers = params.getTensorsList();
        ByteBuffer[] newWeights = new ByteBuffer[layers.size()];
        for (int i = 0; i < layers.size(); ++i) {
            newWeights[i] = ByteBuffer.wrap(layers.get(i).toByteArray());
        }
        return newWeights;
    }

    private static List<ByteString> paramsToByteStringList(ByteBuffer[] params) {
        List<ByteString> layers = new ArrayList<ByteString>();
        for (int i=0; i < params.length; i++) {
            layers.add(ByteString.copyFrom(params[i]));
        }
        return layers;
    }

    private static HashMap<String, Scalar> wrapConfig(Map<String, flwr.proto.Scalar> config) {
        HashMap<String, Scalar> res = new HashMap<String, Scalar>();
        for (Map.Entry<String, flwr.proto.Scalar> i : config.entrySet()) {
            res.put(i.getKey(), new Scalar(i.getValue()));
        }
        return res;
    }
}
