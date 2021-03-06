/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator.buttons;

import org.solovyev.android.Check;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public enum CppSpecialButton {

    history("history"),
    cursor_right("▷"),
    cursor_left("◁"),
    settings("settings"),
    settings_widget("settings_widget"),
    like("like"),
    memory("memory"),
    erase("erase"),
    paste("paste"),
    copy("copy"),
    equals("="),
    clear("clear"),
    functions("functions"),
    open_app("open_app"),
    vars("vars"),
    operators("operators");

    @Nonnull
    private static Map<String, CppSpecialButton> buttonsByActions = new HashMap<>();

    @Nonnull
    public final String action;

    CppSpecialButton(@Nonnull String action) {
        this.action = action;
    }

    @Nullable
    public static CppSpecialButton getByAction(@Nonnull String action) {
        initButtonsByActionsMap();
        return buttonsByActions.get(action);
    }

    private static void initButtonsByActionsMap() {
        Check.isMainThread();
        if (!buttonsByActions.isEmpty()) {
            return;
        }
        for (CppSpecialButton specialButton : values()) {
            buttonsByActions.put(specialButton.action, specialButton);
        }
    }

    @Nonnull
    public String getAction() {
        return action;
    }
}
