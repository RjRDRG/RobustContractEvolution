package com.rce.editor.gui;

import com.rce.common.structures.ContractEvolution;
import com.rce.editor.gui.utils.ButtonColumn;
import com.rce.editor.gui.utils.JGridBagPanel;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class EndpointPanel extends JPanel {

    final ContractEvolution evolution;
    final JLabel la0;               final JLabel la1;
    final JList<String> ls0;       final JList<String> ls1;
    final JButton bt0;

    final JEndpointTable t0;

    public JButton next;

    public EndpointPanel(
            String title0, final Map<String,Set<String>> elements0,
            String title1, final Map<String,Set<String>> elements1,
            final Map<String,Set<String>> pairs,
            ContractEvolution evolution) {
        this.evolution = evolution;
        setLayout(new BorderLayout());
        JGridBagPanel gp0 = new JGridBagPanel();
        add(gp0,BorderLayout.CENTER);

        //--------------------------------------------------------------------------------------------------------------

        la0 = new JLabel();
        la1 = new JLabel();

        ls0 = new JList<>();
        ls1 = new JList<>();

        bt0 = new JButton();

        t0 = new JEndpointTable();

        next = new JButton();

        //--------------------------------------------------------------------------------------------------------------

        la0.setText(title0);
        la0.setAlignmentX(SwingConstants.CENTER);

        gp0.load(0,0, la0).removeScaleY().add();

        //--------------------------------------------------------------------------------------------------------------

        DefaultListModel<String> m0 = new DefaultListModel<>();
        m0.addAll(elements0.keySet());
        ls0.setModel(m0);
        ls0.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ls0.setFocusable(false);
        JScrollPane s0 = new JScrollPane(ls0);

        gp0.load(0,1, s0).add();

        //--------------------------------------------------------------------------------------------------------------

        la1.setText(title1);
        la1.setAlignmentX(SwingConstants.CENTER);

        gp0.load(1,0, la1).removeScaleY().add();

        //--------------------------------------------------------------------------------------------------------------

        DefaultListModel<String> m1 = new DefaultListModel<>();
        m1.addAll(elements1.keySet());
        ls1.setModel(m1);
        ls1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ls1.setFocusable(false);
        JScrollPane s1 = new JScrollPane(ls1);

        gp0.load(1,1, s1).add();

        //--------------------------------------------------------------------------------------------------------------

        bt0.setText("Pair Endpoints");
        bt0.setFocusable(false);
        bt0.addActionListener(e -> {
            if(!(ls0.getSelectedIndex()>=0 && ls1.getSelectedIndex()>=0))
                return;

            DefaultListModel<String> lm0 = (DefaultListModel<String>) ls0.getModel();
            DefaultListModel<String> lm1 = (DefaultListModel<String>) ls1.getModel();

            String endpoint = ls0.getSelectedValue();
            String priorEndpoint = ls1.getSelectedValue();
            Set<String> responses = Optional.ofNullable(elements0.get(endpoint)).orElse(pairs.get(endpoint));
            Set<String> responseOptions = Optional.ofNullable(elements1.get(priorEndpoint)).orElse(pairs.get(priorEndpoint));

            buildRow(t0,endpoint,priorEndpoint,responses,responseOptions);

            lm0.remove(ls0.getSelectedIndex());
            lm1.remove(ls1.getSelectedIndex());
        });

        gp0.load(0,2, bt0).setWidth(2).removeScaleY().add();

        //--------------------------------------------------------------------------------------------------------------

        List<String> responses = List.of(elements0.values(), elements1.values(), pairs.values()).stream()
                .flatMap(Collection::stream)
                .flatMap(Set::stream)
                .distinct()
                .sorted()
                .map(r -> "Response : " + r)
                .collect(Collectors.toList());

        String[] columnNames = List.of(
                List.of("Endpoint", "Prior Endpoint", "Request"),
                responses,
                List.of("")
        ).stream().flatMap(Collection::stream).toArray(String[]::new);

        Action removeRow = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                int row = Integer.parseInt( e.getActionCommand() );
                ((DefaultListModel<String>) ls0.getModel()).addElement((String) t0.getModel().getValueAt(row,0));
                ((DefaultListModel<String>) ls1.getModel()).addElement((String) t0.getModel().getValueAt(row,1));
            }
        };

        t0.buildTable(columnNames, Set.of(0,1,2), removeRow);

        for(Map.Entry<String,Set<String>> entry : pairs.entrySet()) {
            buildRow(t0, entry.getKey(), entry.getKey(), entry.getValue(), entry.getValue());
        }

        JScrollPane s2 = new JScrollPane(t0);

        gp0.load(0,3, s2).setTopPad(10).setWidth(2).add();

        next.setText("Next");
        next.setFocusable(false);

        gp0.load(1,4, next).removeScaleY().removeScaleX().setAnchorRight().setTopPad(5).add();

        //--------------------------------------------------------------------------------------------------------------


    }

    private void buildRow(JEndpointTable table, String endpoint, String previousEndpoint, Set<String> responses, Set<String> responseOptions) {
        String[] row = new String[table.getColumnCount()];
        row[0] = endpoint;
        row[1] = previousEndpoint;
        row[2] = "Request";

        for(int i=3; i<table.getColumnCount()-1; i++) {
            String columnName = table.getColumnName(i).replace("Response : ","");
            if(responses.contains(columnName)) {
                if(responseOptions.contains(columnName))
                    row[i] = columnName;
                else
                    row[i] = "MISSING";
            }
            else
                row[i] = JEndpointTable.BLANK;
        }

        row[table.getColumnCount()-1]="X";

        Set<Integer> columnsWithOptions = new HashSet<>();
        for(int i=3; i<table.getColumnCount()-1; i++) {
            if(responses.contains(table.getColumnName(i).replace("Response : ","")))
                columnsWithOptions.add(i);
        }
        table.addRow(row, responseOptions, columnsWithOptions);
    }

    public ResolutionPanel getNextPanel() {
        return new ResolutionPanel(evolution, t0.getValues(), t0.getColumnNames());
    }
}

class JEndpointTable extends JTable {

    public static final String BLANK = "-";
    public static final String MISSING = "MISSING";

    private final EndpointTableModel model;

    private final DefaultTableCellRenderer missingCellRenderer;

    private Set<Integer> nonEditableColumns;

    public JEndpointTable() {
        super();
        model = new EndpointTableModel();

        missingCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setForeground(new Color(199, 84, 80));
                Font f = l.getFont();
                l.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
                return l;
            }
        };
        missingCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return !nonEditableColumns.contains(column) && !model.getValueAt(row,column).equals(BLANK);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        String value = (String) model.getValueAt(row,column);
        if (value.equals(MISSING))
            return missingCellRenderer;
        else
            return super.getCellRenderer(row,column);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        String[] options = model.getOptions(row, column);
        if (options != null) {
            JComboBox<String> comboBox = new JComboBox<>(options);
            comboBox.setFocusable(false);
            comboBox.addActionListener((e)-> {
                model.setValueAt(comboBox.getSelectedItem(), row, column);
            });
            DefaultCellEditor defaultCellEditor = new DefaultCellEditor(comboBox);
            comboBox.addPopupMenuListener(new PopupMenuListener() {
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
                public void popupMenuCanceled(PopupMenuEvent e) {
                    defaultCellEditor.cancelCellEditing();
                }
            });
            return defaultCellEditor;
        } else {
            return super.getCellEditor(row, column);
        }
    }

    public void buildTable(String[] columnNames, Set<Integer> nonEditableColumns, Action additionalRemoveRowAction) {
        this.nonEditableColumns = nonEditableColumns;
        model.setColumns(columnNames);
        setModel(model);

        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setResizingAllowed(false);
        setCellSelectionEnabled(false);
        setFocusable(false);
        setShowGrid(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        setDefaultRenderer(Object.class, centerRenderer);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment( SwingConstants.LEFT );
        getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        getColumnModel().getColumn(1).setCellRenderer(leftRenderer);

        Action removeRow = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                int modelRow = Integer.parseInt( e.getActionCommand() );
                model.removeRow(modelRow);
            }
        };

        ButtonColumn buttonColumn = new ButtonColumn(this, model.getColumnCount()-1, new Color(199, 84, 80), additionalRemoveRowAction, removeRow);
        buttonColumn.setMnemonic(KeyEvent.VK_D);
        getColumnModel().getColumn( model.getColumnCount()-1).setMaxWidth(30);
        getColumnModel().getColumn( model.getColumnCount()-1).setMinWidth(30);
    }

    public void addRow(String[] row, Set<String> options, Set<Integer> columns) {
        model.addRow(row,options,columns);
    }

    public String[][] getValues() {
        String[][] values = new String[model.getRowCount()][model.getColumnCount()-1];
        for(int i=0; i<model.getRowCount(); i++) {
            for(int j=0; j<model.getColumnCount()-1; j++) {
                String mVal = (String) model.getValueAt(i,j);
                values[i][j] = mVal.equals(BLANK) ? null : mVal;
            }
        }
        return values;
    }

    public String[] getColumnNames() {
        String[] c = new String[model.getColumnCount()-1];
        for(int i=0; i<model.getColumnCount()-1; i++) {
            c[i] = model.getColumnName(i);
        }
        return c;
    }
}

class EndpointTableModel extends DefaultTableModel {

    List<Set<String>> rowOptions;
    List<Set<Integer>> columnsWithOptions;

    public EndpointTableModel() {
        super();
        rowOptions = new ArrayList<>();
        columnsWithOptions = new ArrayList<>();
    }

    public void setColumns(String[] columnNames) {
        setColumnIdentifiers(columnNames);
    }

    public void addRow(String[] row, Set<String> options, Set<Integer> columns) {
        super.addRow(row);
        rowOptions.add(options);
        columnsWithOptions.add(columns);
    }

    @Override
    public void removeRow(int row) {
        super.removeRow(row);
        rowOptions.remove(row);
        columnsWithOptions.remove(row);
    }

    public String[] getOptions(int row, int column) {
        if (columnsWithOptions.get(row).contains(column)) {
            return rowOptions.get(row).toArray(new String[0]);
        }
        else {
            return null;
        }
    }
}
