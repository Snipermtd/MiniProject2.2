package edu.augusta.sccs.trivia.server;

import edu.augusta.sccs.trivia.QuestionsReply;
import edu.augusta.sccs.trivia.QuestionsRequest;
import edu.augusta.sccs.trivia.TriviaQuestionsGrpc;
import edu.augusta.sccs.trivia.cassandra.QuestionRepository;
import edu.augusta.sccs.trivia.cassandra.ServerQuestion;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ServerEndpoint {

    private static final Logger logger = Logger.getLogger(ServerEndpoint.class.getName());


    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new TrivaQuestionImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        System.out.println("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    if (server != null) {
                        server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }


    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            System.out.println("going to await termination");
            server.awaitTermination();
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        final ServerEndpoint server = new ServerEndpoint();
        server.start();
        server.blockUntilShutdown();
    }

    static class TrivaQuestionImpl extends TriviaQuestionsGrpc.TriviaQuestionsImplBase {

        @Override
        public void getQuestions(QuestionsRequest req, StreamObserver<QuestionsReply> responseObserver) {
            QuestionRepository questionRepo = new QuestionRepository("questions", 9042, "datacenter1");


            List<ServerQuestion> questions = questionRepo.getQuestionsByDifficulty(req.getDifficulty(), req.getNumberOfQuestions());


            QuestionsReply.Builder builder = QuestionsReply.newBuilder();
            for(ServerQuestion q: questions) {
                builder.addQuestions(QuestionRepository.convertToQuestion(q));
            }

            QuestionsReply reply = builder.build();

            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

    }
}

