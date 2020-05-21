/*
 * This file is part of JNotes. Copyright (C) 2020  Joy Chakravarty
 * 
 * JNotes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JNotes is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JNotes.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * 
 */
package com.jc.jnotes.viewcontroller;

import java.util.function.BiConsumer;

import com.jc.jnotes.model.NoteEntry;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Editable TableCell (in a TableView) which can be saved with Ctrl+S
 * 
 * @author Joy C
 */
public class SaveEnabledTableCell extends TableCell<NoteEntry, String> {

    private TextField textField;
    private BiConsumer<String, Integer> saveOnEditBiConsumer;
    private int colIndex;

    public SaveEnabledTableCell(BiConsumer<String, Integer> saveOnEditBiConsumer, int colIndex) {
        this.saveOnEditBiConsumer = saveOnEditBiConsumer;
        this.colIndex = colIndex;
    }

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText((String) getItem());
        setGraphic(null);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.toString());
        }
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);

        int row = getIndex();
        NoteEntry note = getTableView().getItems().get(row);
        if (colIndex == 1) {
            textField.setText(note.getValue());
        }

        textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.S && event.isShortcutDown()) {
                event.consume();
                System.out.println("origStr = " + getString() + "  new string" + textField.getText());
                // if(!getString().equals(textField.getText())) {
                saveOnEditBiConsumer.accept(textField.getText(), colIndex);
                // }
            }
        });
    }

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}
