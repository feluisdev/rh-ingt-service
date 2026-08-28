package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 *
 * Phase 111 raised this from 14 to 15.
 *
 * <p><b>What ENFORCED means changed in Phase 115 (2026-08-26, plan 115-08).</b> Before this
 * phase, every {@code ACTOR-CHECK: ENFORCED} handler checked the caller itself, by calling
 * {@code currentEmployeeResolver.resolve()} and comparing the result against the aggregate. Plans 115-05 and
 * 115-06 moved the check for two of these handlers ({@code AssignMeritRatingCommandHandler},
 * {@code CloseEvaluationsCommandHandler}) to a permission guard on the corresponding
 * {@code ComplianceController} method instead -- there is nothing left in those handlers to
 * resolve the caller against, because the guard now runs before the handler is invoked at all.
 * Their {@code ACTOR-CHECK: ENFORCED} comments were rewritten to name the permission and the
 * guarded controller method, which is the correct form now, not a mistake. This test's
 * {@link #enforcedHandlersActuallyEnforceTheirDisposition()} therefore accepts two legitimate
 * forms of ENFORCED: the original in-handler resolution (still required for the handlers that
 * compare against an aggregate they hold), and a controller-level permission guard named in the
 * reason text and verified against the real {@code @PreAuthorize} annotation in
 * {@code ComplianceController.java}. A handler that declares ENFORCED and has neither still
 * fails the suite -- the two-forms recognition does not weaken what this test measures.
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

  /**
   * Matches an ACTOR-CHECK reason that claims the second legitimate ENFORCED form: a permission
   * guard living on the controller instead of a resolve-and-compare inside the handler. Only
   * matches the exact phrasing the Phase 115 handlers use -- a reason that mentions a permission
   * or a controller method some other way still falls through to the resolve-call check below,
   * which is the conservative direction (a handler cannot silently claim the new form).
   */
  private static final Pattern CONTROLLER_PERMISSION_REFERENCE_PATTERN =
      Pattern.compile("guarded by @PreAuthorize on ComplianceController#(\\w+)");

  /**
   * Matches a {@code @PreAuthorize} annotation that calls {@code checkPermission}, followed --
   * with anything except another {@code @PreAuthorize} in between -- by the {@code public}
   * method it guards. Used to verify that an ACTOR-CHECK reason naming
   * {@code ComplianceController#someMethod} as permission-guarded is telling the truth, not
   * just asserting it in a comment nobody checks.
   */
  private static final Pattern PRE_AUTHORIZE_PERMISSION_METHOD_PATTERN = Pattern.compile(
      // [^"]*, not [^)]* -- the SpEL argument nests parens itself
      // (T(Permission).SOME_CONSTANT), so a class that excludes ")" instead of the closing
      // quote stops at the first nested ")" and never matches at all.
      "@PreAuthorize\\(\"@igrpAuthorization\\.checkPermission\\([^\"]*\\)\"\\)"
          + "(?:(?!@PreAuthorize\\().)*?"
          + "public\\s+\\S+(?:<[^>]*>)?\\s+(\\w+)\\s*\\(",
      Pattern.DOTALL);

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

  /**
   * The {@code ComplianceController} method names that carry a
   * {@code @PreAuthorize(...checkPermission...)} guard, derived from the controller source
   * itself -- not from any handler's say-so. This is what
   * {@link #enforcedHandlersActuallyEnforceTheirDisposition()} checks an ACTOR-CHECK reason's
   * claim against.
   */
  private static Set<String> derivePermissionGuardedControllerMethods() {
    String controllerText = readFile(CONTROLLER_PATH);
    Set<String> methods = new HashSet<>();
    Matcher matcher = PRE_AUTHORIZE_PERMISSION_METHOD_PATTERN.matcher(controllerText);
    while (matcher.find()) {
      methods.add(matcher.group(1));
    }
    return methods;
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
    assertEquals(15, commandNames.size(),
        "Expected exactly 15 distinct `new XxxCommand(` usages in ComplianceController.java "
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

  /**
   * Two legitimate forms of ENFORCED, since Phase 115 (2026-08-26, plan 115-08 -- see the class
   * javadoc above for why the semantics changed):
   *
   * <ol>
   *   <li>The handler resolves the current employee itself and compares it against the
   *       aggregate ({@link #RESOLVE_CALL} called outside a comment). This is the original
   *       form, still required for every handler whose reason does not claim the second form.
   *   <li>The reason names a {@code ComplianceController} method as guarded by
   *       {@code @PreAuthorize}, and that method genuinely carries a
   *       {@code @PreAuthorize(...checkPermission...)} annotation in the controller source. The
   *       claim is checked against the controller file, not taken on trust -- a handler cannot
   *       declare this form for a method that has no such guard.
   * </ol>
   *
   * A handler that declares ENFORCED and satisfies neither still fails this test: recognising a
   * second legitimate form does not relax the requirement that every ENFORCED handler back its
   * claim with real code.
   */
  @Test
  void enforcedHandlersActuallyEnforceTheirDisposition() {
    Set<String> permissionGuardedMethods = derivePermissionGuardedControllerMethods();
    for (String commandName : deriveCommandNames()) {
      Path handlerPath = handlerPathFor(commandName);
      Matcher marker = requireMarker(handlerPath);
      if (!"ENFORCED".equals(marker.group(1))) {
        continue;
      }
      String reason = marker.group(2);
      Matcher controllerRef = CONTROLLER_PERMISSION_REFERENCE_PATTERN.matcher(reason);
      if (controllerRef.find()) {
        String method = controllerRef.group(1);
        assertTrue(permissionGuardedMethods.contains(method), handlerPath.getFileName()
            + " ACTOR-CHECK reason claims ComplianceController#" + method
            + " is guarded by @PreAuthorize(...checkPermission...), but that method carries no "
            + "such annotation in " + CONTROLLER_PATH
            + " -- the declaration says one thing, the code does another");
        continue;
      }
      boolean callsResolve = nonCommentLines(handlerPath).stream()
          .anyMatch(line -> line.contains(RESOLVE_CALL));
      assertTrue(callsResolve, handlerPath.getFileName()
          + " declares ACTOR-CHECK: ENFORCED but neither calls " + RESOLVE_CALL
          + " outside a comment nor names a ComplianceController permission guard in its "
          + "reason -- the declaration says one thing, the code does another");
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
