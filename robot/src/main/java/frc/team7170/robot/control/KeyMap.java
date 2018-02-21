package frc.team7170.robot.control;

import java.util.HashMap;


abstract class KeyMap {

    HashMap<KeyBindings.Action, Control.HIDButtonAccessor> buttons = new HashMap<>();

    HashMap<KeyBindings.Action, Control.HIDAxisAccessor> axes = new HashMap<>();

    HashMap<KeyBindings.Action, Control.HIDPOVAccessor> POV = new HashMap<>();
}
