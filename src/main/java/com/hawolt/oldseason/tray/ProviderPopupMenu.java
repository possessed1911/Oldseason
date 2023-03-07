package com.hawolt.oldseason.tray;

import com.hawolt.oldseason.Main;
import com.hawolt.oldseason.web.Provider;

import java.awt.*;
import java.awt.event.ItemEvent;

/**
 * Created: 07/03/2023 05:54
 * Author: Twitter @hawolt
 **/

public class ProviderPopupMenu extends PopupMenu {
    public static String selection;

    public ProviderPopupMenu() {
        super("Provider");
        for (Provider provider : Provider.values()) {
            CheckboxMenuItem item = new CheckboxMenuItem(provider.name());
            item.addItemListener(e -> {
                boolean selected = e.getStateChange() == ItemEvent.SELECTED;
                Main.automatic.setState(selected);
                Main.automatic.setEnabled(selected);
                int items = getItemCount();
                for (int i = 0; i < items; i++) {
                    CheckboxMenuItem menuItem = (CheckboxMenuItem) getItem(i);
                    if (menuItem.getLabel().equals(e.getItem())) continue;
                    menuItem.setState(false);
                }
                selection = selected ? e.getItem().toString() : null;
            });
            add(item);
        }
        getItem(0).setEnabled(true);
    }
}
