package flwr;

import java.nio.ByteBuffer;
import java.util.Map;
import flwr.Scalar;

public interface TFLiteClient {

    public ByteBuffer[] getParameters();

    public FitRes fit(ByteBuffer[] parameters, Map<String, Scalar> config);

    public EvaluateRes evaluate(ByteBuffer[] parameters, Map<String, Scalar> config);

    public static class FitRes {
        public ByteBuffer[] parameters;
        public long num_examples;
        public Map<String, Scalar> metrics;
    }

    public static class EvaluateRes {
        public long num_examples;
        public float loss;
        public Map<String, Scalar> metrics;
    }
}
