package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * This test IS Phase 101's criterion 4 (SIA-04): before it existed, a command handler with no
 * actor check was indistinguishable from one nobody had gotten around to writing yet. A new
 * operation added to {@code ComplianceController} that dispatches a new command fails this
 * suite until the corresponding handler declares its ACTOR-CHECK disposition -- ENFORCED or
 * NOT-REQUIRED, never silence.
 *
 * What this test does NOT see, and must not be read as guaranteeing: a command constructed
 * through a builder/factory instead of a literal {@code new XxxCommand(...)} call in
 * {@code ComplianceController}, and a handler reachable through some path other than this one
 * controller. What it guarantees is completeness of the declaration within the surface it
 * derives -- nothing beyond that.
 */
class SiadapCommandActorCheckCoverageTest {

  private static final Path CONTROLLER_PATH = Path.of(
      "src", "main", "java", "cv", "igrp", "RH_Service", "sigdi", "interfaces", "rest",
      "ComplianceController.java");

  private static final Path HANDLERS_DIR = Path.of(
      "src", "main", "java", "cv", "igrp", "RH_Service", "sigdi", "application", "commands");

  private static final Pattern COMMAND_USAGE_PATTERN =
      Pattern.compile("new\\s+([A-Z][A-Za-z0-9]*Command)\\s*\\(");

  private static final Pattern ACTOR_CHECK_PATTERN =
      Pattern.compile("//\\s*ACTOR-CHECK:\\s*(ENFORCED|NOT-REQUIRED)\\s*--\\s*(\\S.*)");

  private static final String RESOLVE_CALL = "currentEmployeeResolver.resolve()";

  private static String readFile(Path path) {
    if (!Files.isRegularFile(path)) {
      fail("Expected file not found: " + path.toAbsolutePath());
    }
    try {
      return Files.readString(path);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read " + path.toAbsolutePath(), e);
    }
  }

  /**
   * The command names {@code ComplianceController} dispatches via a literal
   * {@code new XxxCommand(...)} call, collected into a {@link TreeSet} so a regex that stops
   * matching produces an empty set rather than a silently-shrunk one -- see
   * {@link #theDerivedCommandSetHasTheExpectedSize()}.
   */
  private static TreeSet<String> deriveCommandNames() {
    String controllerText = readFile(CONTROLLER_PATH);
    TreeSet<String> names = new TreeSet<>();
    Matcher matcher = COMMAND_USAGE_PATTERN.matcher(controllerText);
    while (matcher.find()) {
      names.add(matcher.group(1));
    }
    return names;
  }

  private static Path handlerPathFor(String commandName) {
    return HANDLERS_DIR.resolve(commandName + "Handler.java");
  }

  /**
   * Lines of the handler file with comment lines discarded (leading {@code //} or {@code *}).
   * Without this, the ACTOR-CHECK marker line itself -- or any explanatory comment mentioning
   * {@link #RESOLVE_CALL} -- would count as code, making the ENFORCED/NOT-REQUIRED assertions
   * circular.
   */
  private static List<String> nonCommentLines(Path path) {
    return readFile(path).lines()
        .filter(line -> {
          String trimmed = line.stripLeading();
          return !trimmed.startsWith("//") && !trimmed.startsWith("*");
        })
        .collect(Collectors.toList());
  }

  private static Matcher requireMarker(Path handlerPath) {
    Matcher matcher = ACTOR_CHECK_PATTERN.matcher(readFile(handlerPath));
    if (!matcher.find()) {
      fail(handlerPath.getFileName() + " has no ACTOR-CHECK marker (or it does not match the "
          + "fixed regex `// ACTOR-CHECK: (ENFORCED|NOT-REQUIRED) -- reason`)");
    }
    return matcher;
  }

  @Test
  void theDerivedCommandSetHasTheExpectedSize() {
    TreeSet<String> commandNames = deriveCommandNames();
    assertEquals(14, commandNames.size(),
        "Expected exactly 14 distinct `new XxxCommand(` usages in ComplianceController.java "
            + "-- a regex that stopped matching would silently shrink this set. Found: "
            + commandNames);
  }

  @Test
  void everyComplianceCommandHandlerDeclaresItsActorCheck() {
    for (String commandName : deriveCommandNames()) {
      Path handlerPath = handlerPathFor(commandName);
      if (!Files.isRegularFile(handlerPath)) {
        fail("Handler file not found for command " + commandName + ": expected at "
            + handlerPath.toAbsolutePath());
      }
      long markerCount = ACTOR_CHECK_PATTERN.matcher(readFile(handlerPath)).results().count();
      assertEquals(1, markerCount,
          handlerPath.getFileName() + " must declare exactly one ACTOR-CHECK marker, found "
              + markerCount);
    }
  }

  @Test
  void enforcedHandlersActuallyResolveTheCurrentEmployee() {
    for (String commandName : deriveCommandNames()) {
      Path handlerPath = handlerPathFor(commandName);
      Matcher marker = requireMarker(handlerPath);
      if (!"ENFORCED".equals(marker.group(1))) {
        continue;
      }
      boolean callsResolve = nonCommentLines(handlerPath).stream()
          .anyMatch(line -> line.contains(RESOLVE_CALL));
      assertTrue(callsResolve, handlerPath.getFileName()
          + " declares ACTOR-CHECK: ENFORCED but never calls " + RESOLVE_CALL
          + " outside a comment -- the declaration says one thing, the code does another");
    }
  }

  @Test
  void notRequiredHandlersDoNotSilentlyCheck() {
    for (String commandName : deriveCommandNames()) {
      Path handlerPath = handlerPathFor(commandName);
      Matcher marker = requireMarker(handlerPath);
      if (!"NOT-REQUIRED".equals(marker.group(1))) {
        continue;
      }
      boolean callsResolve = nonCommentLines(handlerPath).stream()
          .anyMatch(line -> line.contains(RESOLVE_CALL));
      assertFalse(callsResolve, handlerPath.getFileName()
          + " declares ACTOR-CHECK: NOT-REQUIRED but calls " + RESOLVE_CALL
          + " outside a comment -- declaration and code disagree in the other direction");
    }
  }

  @Test
  void everyDispositionCarriesAWrittenReason() {
    for (String commandName : deriveCommandNames()) {
      Path handlerPath = handlerPathFor(commandName);
      Matcher marker = requireMarker(handlerPath);
      String reason = marker.group(2);
      assertTrue(reason.length() >= 40, handlerPath.getFileName()
          + " ACTOR-CHECK reason is only " + reason.length()
          + " characters, needs at least 40: \"" + reason + "\"");
    }
  }
}
