package flwr;

import android.os.AsyncTask;
import android.util.Log;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;

import flwr.Client;
import flwr.proto.FlowerServiceGrpc;
import flwr.proto.FlowerServiceGrpc.FlowerServiceBlockingStub;
import flwr.proto.FlowerServiceGrpc.FlowerServiceStub;
import flwr.proto.ClientMessage;
import flwr.proto.ServerMessage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * Interface between GRPC and Client.
 */
public class AndroidConnection {
    private static String TAG = "flwr.AndroidConnection";

    private static final int GRPC_MAX_MESSAGE_LENGTH = 512*1024*1024;

    private ManagedChannel channel;
    private Client client;

    //TODO add support for setting grpc max message length
    public AndroidConnection(String hostname, int port, Client client) {
        channel = ManagedChannelBuilder
            .forAddress(hostname, port)
            .maxInboundMessageSize(GRPC_MAX_MESSAGE_LENGTH)
            .usePlaintext()
            .build();
        this.client = client;
    }

    public void listen() {
        new GrpcTask(new FlowerServiceRunnable(), channel, client).execute();
    }

    private static class GrpcTask extends AsyncTask<Void, Void, String> {
        private final GrpcRunnable grpcRunnable;
        private final ManagedChannel channel;
        private final Client client;

        GrpcTask(GrpcRunnable grpcRunnable, ManagedChannel channel, Client client) {
            this.grpcRunnable = grpcRunnable;
            this.channel = channel;
            this.client = client;
        }

        @Override
        protected String doInBackground(Void... nothing) {
            try {
                grpcRunnable.run(FlowerServiceGrpc.newBlockingStub(channel), FlowerServiceGrpc.newStub(channel), client);
                return "Connection to the FL server successful \n";
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();
                return "Failed to connect to the FL server \n" + sw;
            }
        }

        //TODO this UI stuff should not be in the flwr class
        /*@Override
        protected void onPostExecute(String result) {
            MainActivity activity = activityReference;
            if (activity == null) {
                return;
            }
            activity.setResultText(result);
            activity.trainButton.setEnabled(false);
        }*/
    }

    private interface GrpcRunnable {
        void run(FlowerServiceBlockingStub blockingStub, FlowerServiceStub asyncStub, Client client) throws Exception;
    }
    
    private static class FlowerServiceRunnable implements GrpcRunnable {
        private Throwable failed;
        private StreamObserver<ClientMessage> requestObserver;

        @Override
        public void run(FlowerServiceBlockingStub blockingStub, FlowerServiceStub asyncStub, Client client)
                throws Exception {
             join(asyncStub, client);
        }

        private void join(FlowerServiceStub asyncStub, Client client)
                throws InterruptedException, RuntimeException {
            final CountDownLatch finishLatch = new CountDownLatch(1);
            requestObserver = asyncStub.join(
                            new StreamObserver<ServerMessage>() {
                                @Override
                                public void onNext(ServerMessage msg) {
                                    handleMessage(msg, client);
                                }

                                @Override
                                public void onError(Throwable t) {
                                    failed = t;
                                    finishLatch.countDown();
                                    Log.e(TAG, t.getMessage());
                                }

                                @Override
                                public void onCompleted() {
                                    finishLatch.countDown();
                                    Log.e(TAG, "Done");
                                }
                            });
        }

        private void handleMessage(ServerMessage message, Client client) {
            try {
                ByteBuffer[] weights;
                ClientMessage c = null;
                if (message.hasGetParameters()) {
                    Log.e(TAG, "Handling GetParameters");
                    ClientMessage.ParametersRes res = client.getParameters();
                    c = ClientMessage.newBuilder().setParametersRes(res).build();
                } else if (message.hasFitIns()) {
                    Log.e(TAG, "Handling FitIns");
                    ClientMessage.FitRes res = client.fit(message.getFitIns());
                    c = ClientMessage.newBuilder().setFitRes(res).build();
                } else if (message.hasEvaluateIns()) {
                    Log.e(TAG, "Handling EvaluateIns");
                    ClientMessage.EvaluateRes res = client.evaluate(message.getEvaluateIns());
                    c = ClientMessage.newBuilder().setEvaluateRes(res).build();
                }
                requestObserver.onNext(c);
                c = null;
            }
            catch (Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

}
