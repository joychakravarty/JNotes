package com.jc.jnotes.viewcontroller;

import java.util.function.BiConsumer;

import com.jc.jnotes.model.NoteEntry;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
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
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
            textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.S && event.isShortcutDown()) {
                    event.consume();
                    System.out.println("origStr = " + getString() + "  new string" + textField.getText());
                    //if(!getString().equals(textField.getText())) {
                        saveOnEditBiConsumer.accept(textField.getText(), colIndex);
                    //}
                }
            });
        }
 
        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }
