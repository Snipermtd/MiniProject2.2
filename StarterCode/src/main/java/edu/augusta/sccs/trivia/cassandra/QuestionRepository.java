package edu.augusta.sccs.trivia.cassandra;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import edu.augusta.sccs.trivia.AnswerType;
import edu.augusta.sccs.trivia.Question;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestionRepository {

    private static final String KEYSPACE = "trivia";
    private static final String TABLE_NAME = "questions";

    private InetSocketAddress contactPoint;
    private String localDatacenter;

    public QuestionRepository(String host, int port, String datacenter) {
        contactPoint = new InetSocketAddress(host, port);
        localDatacenter = datacenter;
    }

    private List<ServerQuestion> executeQueryStatement(SimpleStatement statement) {
        CqlIdentifier keyspace = CqlIdentifier.fromCql(KEYSPACE);
        CqlSessionBuilder builder = CqlSession.builder();
        builder.addContactPoint(contactPoint);
        builder.withLocalDatacenter(localDatacenter);
        builder.withKeyspace(keyspace);

        CqlSession session = builder.build();
        statement = statement.setKeyspace(keyspace);
        ResultSet resultSet = session.execute(statement);

        List<Row> rows = resultSet.all();

        List<ServerQuestion> questions = new ArrayList<>();
        for(Row row : rows) {
            questions.add(ServerQuestion.convert(row));
        }

        session.close();
        return questions;
    }

    public List<ServerQuestion> getAllQuestions() {
        Select select = QueryBuilder.selectFrom(TABLE_NAME).all();

        return executeQueryStatement(select.build());
    }

    public List<ServerQuestion> getQuestionsByDifficulty(int difficulty, int numQuestions) {

        Select select = QueryBuilder.selectFrom(TABLE_NAME).all();
        select = select.whereColumn(ServerQuestion.DIFFICULTY).isEqualTo(QueryBuilder.literal(difficulty));
        select = select.limit(numQuestions);

        return executeQueryStatement(select.build());
    }

    public ServerQuestion getQuestionById(UUID questionId) {
        Select select = QueryBuilder.selectFrom(TABLE_NAME).all();
        select = select.whereColumn(ServerQuestion.QUESTION_UUID).isEqualTo(QueryBuilder.literal(questionId));
        return executeQueryStatement(select.build()).get(0);
    }


    public static Question convertToQuestion(ServerQuestion dbQuestion) {
        if(dbQuestion == null) {
            return null;
        }

        return Question.newBuilder()
                    .setUuid(dbQuestion.getQuestionUuid().toString())
                    .setDifficulty(dbQuestion.getDifficulty())
                    .setQuestion(dbQuestion.getQuestion())
                    .setAnswer(dbQuestion.getAnswer())
                    .setAnswerType(AnswerType.forNumber(dbQuestion.getAnswerType()))
                    .addAllChoices(dbQuestion.getListOfChoices())
                    .build();

    }

}
