package com.rce.editor.gui.utils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JViewerPanel<T extends JPanel> extends JPanel {

    private final Map<Integer, T> panels;
    private JPanel active;
    private final int numberOfColumns;
    private final int numberOfRows;

    private final JPanel empty;

    public JViewerPanel(int numberOfColumns, int numberOfRows, String emptyMessage) {
        setLayout(new BorderLayout());
        this.panels = new HashMap<>();

        this.empty = makeEmptyPanel(emptyMessage);
        setActiveEmpty();

        this.numberOfColumns = numberOfColumns;
        this.numberOfRows = numberOfRows;
    }

    public JViewerPanel(int numberOfColumns, int numberOfRows) {
        this(numberOfColumns,numberOfRows, "");
    }

    public int flattenCoordinates(int row, int column) {
        return column + row* numberOfColumns;
    }

    public void addPanel(int row, int column, T panel) {
        panels.put(flattenCoordinates(row,column),panel);
    }

    public void removePanel(int row, int column) {
        int index = flattenCoordinates(row,column);
        if(active.equals(panels.get(index))) {
            setActiveEmpty();
        }
        panels.remove(index);
    }

    public T getActive() {
        if(Objects.equals(active,empty))
            return null;
        else
            return (T) active;
    }

    public void setActive(int row, int column) {
        int index = flattenCoordinates(row,column);
        if(!panels.containsKey(index))
            return;

        if(active != null)
            remove(active);

        active = panels.get(index);
        add(active, BorderLayout.CENTER);
        repaint();
        revalidate();
    }

    public void setActiveEmpty() {
        if(active != null)
            remove(active);

        add(empty, BorderLayout.CENTER);
        this.active = empty;
        repaint();
        revalidate();
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public T getPanel(int row, int column) {
        return panels.get(flattenCoordinates(row,column));
    }

    private JPanel makeEmptyPanel(String message) {
        JPanel empty = new JPanel();
        empty.setBorder(BorderFactory.createLineBorder(new Color(97, 99, 101)));
        empty.setLayout(new GridBagLayout());
        JLabel label = new JLabel(message);
        label.setForeground(new Color(101, 106, 109, 101));
        Font font = label.getFont();
        label.setFont(new Font(font.getName(), Font.BOLD, 20));
        empty.add(label);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.add(Box.createRigidArea(new Dimension(0,1)), BorderLayout.PAGE_END);
        wrapper.add(Box.createRigidArea(new Dimension(0,2)), BorderLayout.PAGE_START);
        wrapper.add(Box.createRigidArea(new Dimension(2,0)), BorderLayout.LINE_START);
        wrapper.add(Box.createRigidArea(new Dimension(2,0)), BorderLayout.LINE_END);
        wrapper.add(empty, BorderLayout.CENTER);

        return wrapper;
    }
}
