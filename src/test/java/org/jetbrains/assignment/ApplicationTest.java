package org.jetbrains.assignment;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {

    @Test
    void locations() {
        final List<Move> input = List.of(
                new Move(Direction.EAST, 1),
                new Move(Direction.NORTH, 3),
                new Move(Direction.EAST, 3),
                new Move(Direction.SOUTH, 5),
                new Move(Direction.WEST, 2)
        );
        final List<Location> expected = List.of(
                new Location(0, 0),
                new Location(1, 0),
                new Location(1, 3),
                new Location(4, 3),
                new Location(4, -2),
                new Location(2, -2)
        );
        final List<Location> actual = new Application().locations(input);
        assertEquals(expected, actual);
    }

    @Test
    void moves() {
        final List<Location> input = List.of(
                new Location(0, 0),
                new Location(1, 0),
                new Location(1, 3),
                new Location(0, 3),
                new Location(0, 0)
        );
        final List<Move> expected = List.of(
                new Move(Direction.EAST, 1),
                new Move(Direction.NORTH, 3),
                new Move(Direction.WEST, 1),
                new Move(Direction.SOUTH, 3)
        );
        final List<Move> actual = new Application().moves(input);
        assertEquals(expected, actual);
    }
}
