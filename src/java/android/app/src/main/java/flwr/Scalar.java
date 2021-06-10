package flwr;

import com.google.protobuf.ByteString;
import static flwr.proto.Scalar.ScalarCase;

/**
 * Wrapper for the Scalar message type.
 */
public class Scalar {

    private flwr.proto.Scalar scalar;
    
    public Scalar(flwr.proto.Scalar scalar) {
        this.scalar = scalar;
    }

    public Scalar(double value) {
        scalar = flwr.proto.Scalar.newBuilder()
            .setDouble(value).build();
    }

    public Scalar(long value) {
        scalar = flwr.proto.Scalar.newBuilder()
            .setSint64(value).build();
    }
    
    public Scalar(boolean value) {
        scalar = flwr.proto.Scalar.newBuilder()
            .setBool(value).build();
    }

    public Scalar(String value) {
        scalar = flwr.proto.Scalar.newBuilder()
            .setString(value).build();
    }

    public Scalar(ByteString value) {
        scalar = flwr.proto.Scalar.newBuilder()
            .setBytes(value).build();
    }

    public ScalarCase type() {
        return scalar.getScalarCase();
    }

    public boolean isDouble() {
        return type() == ScalarCase.DOUBLE;
    }

    public double getDouble() {
        return scalar.getDouble();
    }

    public boolean isSint64() {
        return type() == ScalarCase.SINT64;
    }

    public long getSint64() {
        return scalar.getSint64();
    }

    public boolean isBool() {
        return type() == ScalarCase.BOOL;
    }

    public boolean getBool() {
        return scalar.getBool();
    }

    public boolean isString() {
        return type() == ScalarCase.STRING;
    }

    public String getString() {
        return scalar.getString();
    }

    public boolean isByteString() {
        return type() == ScalarCase.BYTES;
    }

    public ByteString getByteString() {
        return scalar.getBytes();
    }

    public flwr.proto.Scalar getProtoScalar() {
        return scalar;
    }

}
