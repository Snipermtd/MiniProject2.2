package edu.augusta.sccs.trivia.mysql;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*Concrete implementation of our DAO interfaces*/
public class TriviaRepository implements PlayerDao, QuestionResponseDao {

    //hibernate manages database connections with SessionFactory
    private final List<SessionFactory> sessionFactories;

    //Allows our trivia repository to interact with different database configurations
    //there is probably a better way to add configurations as we scale to 10,000,000 users but for 2 configs ...
    public TriviaRepository() {

        this.sessionFactories = new ArrayList<>();
        sessionFactories.add(DbConfig1.getSessionFactory());
        sessionFactories.add(DbConfig2.getSessionFactory());
    }

    private SessionFactory selectSessionFactoryByUuid(UUID uuid){
        int uuidHash = Math.abs(uuid.hashCode());
        int index = uuidHash % sessionFactories.size();

        return sessionFactories.get(index);
    }

    /*Concrete implementation of our database interactions defined in our DAOs*/
    /*@Override
    public List<DbQuestion> getQuestionsByDifficulty(int difficulty, int numQuestions) {
        Session session = sessionFactories.get(0).openSession(); //Open our connection to the database
        session.beginTransaction();  // Ensures atomicity of database operations
        CriteriaBuilder builder = session.getCriteriaBuilder(); // Creates type-safe queries, prevents sql injection
        CriteriaQuery<DbQuestion> cq = builder.createQuery(DbQuestion.class); // cq is a CriteriaQuery object that contains DbQuestions
        Root<DbQuestion> root = cq.from(DbQuestion.class); // tells cs which table to query
        cq.select(root); // select all columns
        cq.where(builder.equal(root.get("difficulty"), difficulty)); // where difficulty matches our parameter
        List<DbQuestion> questions =
                session.createQuery(cq).setMaxResults(numQuestions).getResultList(); // execute our search and limit the results to our numQuestions
        session.getTransaction().commit(); // commit the transaction
        session.close(); // close the session
        return questions; // return result of the operation
    }*/

    /*@Override
    public DbQuestion getSingleQuestionByDifficulty(int difficulty) {
        Session session = sessionFactories.get(0).openSession(); // Open our connection to the database
        session.beginTransaction(); // Ensures atomicity of database operations

        try {
            // Creates type-safe queries, prevents SQL injection
            CriteriaBuilder builder = session.getCriteriaBuilder();

            // Create a CriteriaQuery object that contains DbQuestions
            CriteriaQuery<DbQuestion> cq = builder.createQuery(DbQuestion.class);

            // Specifies the root of the query (which table to query)
            Root<DbQuestion> root = cq.from(DbQuestion.class);

            // Select all columns
            cq.select(root);

            // Filter the query by difficulty
            cq.where(builder.equal(root.get("difficulty"), difficulty));

            // Execute the query and limit the result to 1
            DbQuestion question = session.createQuery(cq).setMaxResults(1).uniqueResult();

            // Commit the transaction
            session.getTransaction().commit();

            return question; // Return the result

        } catch (Exception e) {
            session.getTransaction().rollback(); // Rollback on error
            e.printStackTrace();
            return null;

        } finally {
            session.close(); // Close the session
        }
    }*/


    /*@Override
    public DbQuestion getQuestion(String uuid){
        Session session = sessionFactories.get(0).openSession();
        // Try second database shard if the first one is unreachable
        if (session == null) {
            session = sessionFactories.get(1).openSession();
            if (session == null) {
                throw new IllegalStateException("Could not establish database connection"); //Throw an error if both databases are unreachable
            }
        }
        session.beginTransaction();  // Ensures atomicity of database operations
        CriteriaBuilder builder = session.getCriteriaBuilder(); // Creates type-safe queries, prevents sql injection
        CriteriaQuery<DbQuestion> cq = builder.createQuery(DbQuestion.class); // cq is a CriteriaQuery object that contains DbQuestions
        Root<DbQuestion> root = cq.from(DbQuestion.class); // tells cs which table to query
        cq.select(root); // select all columns
        cq.where(builder.equal(root.get("uuid"), uuid.toString())); // where question uuid matches our parameter
        DbQuestion question = session.createQuery(cq).getSingleResult(); // execute our search
        session.getTransaction().commit(); // commit the transaction
        session.close(); // close the session
        return question; // return result of the operation
    }*/


    @Override
    public DbPlayer findPlayerByUuid(String uuid) {
        Session session = selectSessionFactoryByUuid(UUID.fromString(uuid)).openSession();
        session.beginTransaction();  // Ensures atomicity of database operations
        CriteriaBuilder builder = session.getCriteriaBuilder(); // Creates type-safe queries, prevents sql injection
        CriteriaQuery<DbPlayer> cq = builder.createQuery(DbPlayer.class); // cq is a CriteriaQuery object that contains DbQuestions
        Root<DbPlayer> root = cq.from(DbPlayer.class); // tells cs which table to query
        cq.select(root); // select all columns
        cq.where(builder.equal(root.get("uuid"), uuid)); // where player uuid matches our parameter
        DbPlayer player = session.createQuery(cq).getSingleResult(); // execute our search
        session.getTransaction().commit(); // commit the transaction
        session.close(); // close the session
        return player; // return result of the operation
    }

    @Override
    public void save(DbPlayer player) {
        Session session = selectSessionFactoryByUuid(player.getUuid()).openSession();
        session.beginTransaction();
        session.persist(player);

        session.getTransaction().commit();
        session.close();
    }

    @Override
    public void save(DbQuestionResponse response) {
        Session session = selectSessionFactoryByUuid(response.getPlayer().getUuid()).openSession(); // put response in same database as the player who made it.
        session.beginTransaction();  // Ensures atomicity of database operations

        session.persist(response); // saves response to the database.

        session.getTransaction().commit(); // commit the transaction
        session.close(); // close the session
    }
}
