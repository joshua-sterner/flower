package flwr;

import flwr.proto.ClientMessage.ParametersRes;
import flwr.proto.ClientMessage.FitRes;
import flwr.proto.ClientMessage.EvaluateRes;
import flwr.proto.ServerMessage.FitIns;
import flwr.proto.ServerMessage.EvaluateIns;

public interface Client {
    
    public ParametersRes getParameters();

    public FitRes fit(FitIns ins);

    public EvaluateRes evaluate(EvaluateIns ins);

}
