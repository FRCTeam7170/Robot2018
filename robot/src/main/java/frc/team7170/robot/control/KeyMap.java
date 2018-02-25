package frc.team7170.robot.control;

import java.util.HashMap;


abstract class KeyMap {

    HashMap<KeyBindings.Action, HIDButtonAccessor> buttons = new HashMap<>();

    HashMap<KeyBindings.Action, HIDAxisAccessor> axes = new HashMap<>();

    HashMap<KeyBindings.Action, HIDPOVAccessor> POV = new HashMap<>();
}
