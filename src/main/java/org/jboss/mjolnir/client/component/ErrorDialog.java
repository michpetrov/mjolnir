package org.jboss.mjolnir.client.component;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Error dialog.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class ErrorDialog extends DialogBox {

    public ErrorDialog(Throwable throwable) {
        this(null, throwable);
    }

    public ErrorDialog(String message, Throwable throwable) {
        setText("Error!");

        final HTMLPanel panel = new HTMLPanel("");
        setWidget(panel);

        panel.add(new HTMLPanel("h3", "Something went terribly wrong:"));

        final HTMLPanel messagePara = new HTMLPanel("p", message);
        messagePara.setStyleName("strongText");
        panel.add(messagePara);

        String reason = "Reason: " + throwable.getMessage();
        if (throwable.getCause() != null) {
            reason += ": " + throwable.getCause().getMessage();
        }
        final HTMLPanel reasonPara = new HTMLPanel("p", reason);
        panel.add(reasonPara);

        final HTMLPanel buttonPanel = new HTMLPanel("p", "");
        buttonPanel.setStyleName("textRight");
        panel.add(buttonPanel);

        final Button continueButton = new Button("Continue");
        continueButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ErrorDialog.this.hide();
                ErrorDialog.this.removeFromParent();
            }
        });
        buttonPanel.add(continueButton);
    }
}
