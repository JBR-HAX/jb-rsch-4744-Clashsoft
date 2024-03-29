package org.jetbrains.assignment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
@RestController
public class Application {
    Connection connection;
    private ObjectMapper mapper;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // Using Spring Boot (or any other web framework you choose), implement a simple web service in Java with two endpoints.
    // The application should store the input data and the corresponding result for each request in a database.
    // You can use any data access library/framework and database to implement the service.

    // 1. Endpoint: POST /locations
    // Receives a list of movement operations in JSON format as an input and outputs the coordinates recorded after each move as a JSON document.
    // Assume that the initial location for the robot is always at (0, 0).

    // Example:
    // Request:
    // curl --header "Content-Type: application/json" \
    //  --request POST \
    //  --data '[{"direction":"EAST","steps":1},{"direction":"NORTH","steps":3},{"direction":"EAST","steps":3},
    //         {"direction":"SOUTH","steps":5},{"direction":"WEST","steps":2}]' \
    //http://localhost:8080/locations
    //
    // Response:
    // [{"x":0,"y":0},{"x":1,"y":0},{"x":1,"y":3},{"x":4,"y":3},{"x":4,"y":-2},{"x":2,"y":-2}]

    public Application() throws SQLException {
        String jdbcUrl = "jdbc:sqlite:" + new File("db.sqlite").getAbsolutePath();

        connection = DriverManager.getConnection(jdbcUrl);

        // create table for requests with these columns
        // id - uuid
        // request - json
        // response - json
        // created_at - timestamp

        connection.createStatement().execute("CREATE TABLE IF NOT EXISTS requests (id TEXT PRIMARY KEY, request TEXT, response TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        mapper = new ObjectMapper();
    }

    @PostMapping("/locations")
    public List<Location> locations(@RequestBody List<Move> moves) throws JsonProcessingException, SQLException {

        int x = 0;
        int y = 0;
        final List<Location> locations = new ArrayList<>();
        locations.add(new Location(x, y));

        for (Move move : moves) {
            switch (move.direction()) {
                case NORTH -> y += move.steps();
                case EAST -> x += move.steps();
                case SOUTH -> y -= move.steps();
                case WEST -> x -= move.steps();
            }
            locations.add(new Location(x, y));
        }

        logRequest(moves, locations);

        return locations;
    }

    // 2. Endpoint: POST /moves
    // Receives a list of locations and outputs a list of robot moves to visit all locations. Assume that the starting location of the robot is the first in the input list.
    // Example:
    // Request:
    // curl --header "Content-Type: application/json" \
    //  --request POST \
    //  --data '[{"x": 0, "y": 0}, {"x": 1, "y": 0}, {"x": 1, "y": 3}, {"x": 0, "y": 3}, {"x": 0, "y": 0}]' \
    //  http://localhost:8080/moves
    //
    // Response:
    // [{"direction":"EAST","steps":1},{"direction":"NORTH","steps":3},{"direction":"WEST","steps":1},{"direction":"SOUTH","steps":3}]

    @PostMapping("/moves")
    public List<Move> moves(@RequestBody List<Location> locations) throws SQLException, JsonProcessingException {
        final List<Move> moves = new ArrayList<>();
        Location current = locations.get(0);
        for (int i = 1; i < locations.size(); i++) {
            Location next = locations.get(i);
            int dx = next.x() - current.x();
            int dy = next.y() - current.y();
            if (dx != 0) {
                moves.add(new Move(dx > 0 ? Direction.EAST : Direction.WEST, Math.abs(dx)));
            }
            if (dy != 0) {
                moves.add(new Move(dy > 0 ? Direction.NORTH : Direction.SOUTH, Math.abs(dy)));
            }
            current = next;
        }

        logRequest(locations, moves);

        return moves;
    }

    private void logRequest(Object request, Object response) throws SQLException, JsonProcessingException {
        // insert request and response into the database using a prepared statement!
        final PreparedStatement stmt = connection.prepareStatement("INSERT INTO requests (id, request, response) VALUES (?, ?, ?)");
        stmt.setString(1, UUID.randomUUID().toString());
        stmt.setString(2, mapper.writeValueAsString(request));
        stmt.setString(3, mapper.writeValueAsString(response));
        stmt.execute();
    }
}
