package com.rce.editor.gui;

import com.rce.editor.gui.utils.ButtonColumn;
import com.rce.editor.gui.utils.JGridBagPanel;
import com.rce.editor.gui.utils.JViewerPanel;
import com.rce.common.structures.*;
import com.rce.common.structures.Endpoint;
import com.rce.common.structures.Property;
import com.rce.common.structures.PropertyKey;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.*;

public class ResolutionPanel extends JPanel {

    final JGridBagPanel gp0;

    final JLabel l0;
    final JMessageTable t0;

    final JViewerPanel<JMessagePanel> v0;

    public final JButton back;
    public final JButton submit;

    public ResolutionPanel(String[][] messagePairs, String[] messagesHeader) {
        setLayout(new BorderLayout());
        gp0 = new JGridBagPanel();

        //--------------------------------------------------------------------------------------------------------------

        l0 = new JLabel();
        t0 = new JMessageTable(messagePairs, messagesHeader);

        v0 = new JViewerPanel<>(t0.getColumnCount()-2, messagePairs.length, "EMPTY");

        back = new JButton();
        submit = new JButton();

        //--------------------------------------------------------------------------------------------------------------

        l0.setText("Messages");
        gp0.load(0,0, l0).setWidth(2).removeScaleY().add();

        ListSelectionListener selectionListener = e -> {
            if(e.getValueIsAdjusting())return;

            int c = t0.getSelectedColumn();
            int r = t0.getSelectedRow();

            if(c<2 || t0.getValueAt(r, c) == null) {
                v0.setActiveEmpty();
            }
            else {
                v0.setActive(r, c - 2);
            }
        };

        t0.getSelectionModel().addListSelectionListener(selectionListener);
        t0.getColumnModel().getSelectionModel().addListSelectionListener(selectionListener);

        final JGridBagPanel gpAux0 = new JGridBagPanel();
        gpAux0.load(0,0, t0.getTableHeader()).removeScaleY().add();
        gpAux0.load(0,1, t0).removeScaleY().removeScaleY().add();
        gpAux0.setBorder(BorderFactory.createLineBorder(new Color(97, 99, 101)));

        gp0.load(0,1, gpAux0).removeScaleY().setWidth(2).add();

        //--------------------------------------------------------------------------------------------------------------

        for(int j=0; j<t0.getRowCount(); j++) {
            for (int i = 2; i < t0.getColumnCount(); i++) {
                Endpoint endpoint = Endpoint.fromString((String) t0.getValueAt(j,0));
                Endpoint priorEndpoint = Endpoint.fromString((String) t0.getValueAt(j,1));
                if(t0.getValueAt(j, i) != null) {
                    String message = t0.getColumnName(i).replace("Response : ","");
                    String priorMessage = (String) t0.getValueAt(j,i);

                    Set<Property> properties;
                    Set<Property> priorProperties;
                    if(message.equals("Request")) {
                        properties = EditorFrame.CONTRACT.getRequestProperties(endpoint);
                        priorProperties = EditorFrame.PRIOR_CONTRACT.getRequestProperties(priorEndpoint);
                    }
                    else {
                        properties = EditorFrame.CONTRACT.getResponseProperties(endpoint, message);
                        priorProperties = EditorFrame.PRIOR_CONTRACT.getResponseProperties(priorEndpoint, priorMessage);
                    }

                    v0.addPanel(j, i - 2, new JMessagePanel(properties, priorProperties));
                }
            }
        }

        gp0.load(0,2, v0).setWidth(2).setTopPad(15).add();

        //--------------------------------------------------------------------------------------------------------------

        back.setText("Back");
        gp0.load(0,3, back).removeScaleX().removeScaleY().setAnchorLeft().setTopPad(5).add();

        submit.setText("Submit");
        gp0.load(1,3, submit).removeScaleX().removeScaleY().setAnchorRight().setTopPad(5).add();

        add(gp0,BorderLayout.CENTER);
    }

    public Result getResult() {
        Result result = new Result();

        for(int i=0; i<v0.getNumberOfRows(); i++) {
            Method method = new Method((String) t0.getValueAt(i,0), (String) t0.getValueAt(i,1));
            for (int j=0; j<v0.getNumberOfColumns(); j++) {
                JMessagePanel messagePanel = v0.getPanel(i,j);
                if(messagePanel != null) {
                    List<Parameter> parameters = new ArrayList<>();
                    for (Object[] row : messagePanel.t2.getValues()) {
                        parameters.add(new Parameter(
                                ((Property)row[0]).key.toString(),((Resolution)row[1]).resolution
                        ));
                    }
                    Message message = new Message(t0.getColumnName(j+2).replace("Response : ", ""), (String) t0.getValueAt(i,j+2), parameters);
                    method.addMessage(message);
                }
            }
            result.addMethod(method);
        }

        return result;
    }
}

class JMessagePanel extends JPanel {
    final JGridBagPanel gp0;

    final JGridBagPanel gpa;

    final JLabel la;
    final JPropertyPanel p0;
    final JViewerPanel<JPanel> v0;
    final JButton b0;

    final JLabel l0;
    final JTreePanel t0;

    final JLabel l1;
    final JTreePanel t1;

    final JGridBagPanel gpb;
    final JLabel l3;
    final JSimpleTable t2;

    public JMessagePanel(Set<Property> properties, Set<Property> priorProperties) {
        setLayout(new BorderLayout());
        gp0 = new JGridBagPanel();

        //--------------------------------------------------------------------------------------------------------------

        gpa = new JGridBagPanel(false);

        la = new JLabel();
        p0 = new JPropertyPanel();
        v0 = new JViewerPanel<>(2,1);
        b0 = new JButton();

        l0 = new JLabel();
        t0 = new JTreePanel(properties, true);

        l1 = new JLabel();
        t1 = new JTreePanel(priorProperties, false);

        gpb = new JGridBagPanel(false);
        l3 = new JLabel();
        t2 = new JSimpleTable();

        //--------------------------------------------------------------------------------------------------------------

        TreeSelectionListener selectionListener = e -> {
            Property t0Selected = (Property) Optional.ofNullable(t0.tree.getLastSelectedPathComponent()).map(p -> ((DefaultMutableTreeNode) p).getUserObject()).filter(p0 -> p0 instanceof Property).orElse(null);
            Property t1Selected = (Property) Optional.ofNullable(t1.tree.getLastSelectedPathComponent()).map(p -> ((DefaultMutableTreeNode) p).getUserObject()).filter(p0 -> p0 instanceof Property).orElse(null);

            if(t0Selected == null && t1Selected == null) {
                p0.resetView();
                v0.setActiveEmpty();

                b0.setText("Use Value");
                b0.setEnabled(false);
            }
            else if(t0Selected != null && t1Selected == null) {
                p0.viewProperty(t0Selected);
                v0.setActive(0,0);
                ((JValuePanel)v0.getActive()).setValue(Optional.ofNullable(t0Selected).map(t->t.defaultValue).orElse(""));

                b0.setText("Use Value");
                b0.setEnabled(true);
                return;
            }
            else if(t0Selected == null) {
                p0.resetView();
                v0.setActive(0,1);
                ((JPropertyPanel)v0.getActive()).viewProperty(t1Selected);

                b0.setText("Link Properties");
                b0.setEnabled(false);
                return;
            }
            else {
                p0.viewProperty(t0Selected);
                v0.setActive(0,1);
                ((JPropertyPanel)v0.getActive()).viewProperty(t1Selected);

                b0.setText("Link Properties");
                if(t0Selected.isCompatible(t1Selected))
                    b0.setEnabled(true);
                else
                    b0.setEnabled(false);
            }

            b0.revalidate();
            b0.repaint();
        };

        //--------------------------------------------------------------------------------------------------------------

        la.setText("Resolution Editor");
        gpa.load(0,0, la).removeScaleY().setWidth(2).add();

        gpa.load(0,1, p0).setWeight(0.5f,1).removeScaleY().add();

        v0.addPanel(0,0, new JValuePanel());
        v0.addPanel(0,1, new JPropertyPanel());

        v0.setPreferredSize(p0.getPreferredSize());
        gpa.load(1,1, v0).setWeight(0.5f,1).removeScaleY().add();

        //--------------------------------------------------------------------------------------------------------------

        b0.setText("Use Value");
        b0.setEnabled(false);

        b0.addActionListener(e -> {
            Property t0Selected = (Property) Optional.ofNullable(t0.tree.getLastSelectedPathComponent()).map(p -> ((DefaultMutableTreeNode) p).getUserObject()).filter(p0 -> p0 instanceof Property).orElse(null);
            Property t1Selected = (Property) Optional.ofNullable(t1.tree.getLastSelectedPathComponent()).map(p -> ((DefaultMutableTreeNode) p).getUserObject()).filter(p0 -> p0 instanceof Property).orElse(null);
            Resolution resolution;

            if(t0Selected.solved)
                return;

            if(t1Selected == null) {
                resolution = Resolution.valueResolution(((JValuePanel)v0.getActive()).value.getText());
            }
            else {
                resolution = Resolution.linkResolution(t1Selected.key.toString());
            }

            t2.addRow(new Object[]{t0Selected, resolution, "X"});

            t0Selected.setSolved(true);
            t0.revalidate();
            t0.repaint();
        });

        gpa.load(0,2, b0).removeScaleY().setWidth(2).add();

        //--------------------------------------------------------------------------------------------------------------

        l0.setText("Property Selector");
        gpa.load(0,3, l0).removeScaleY().setTopPad(15).add();

        t0.tree.addTreeSelectionListener(selectionListener);
        t0.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        gpa.load(0,4, t0).add();

        //--------------------------------------------------------------------------------------------------------------

        l1.setText("Prior Properties Selector");
        gpa.load(1,3, l1).removeScaleY().setTopPad(15).add();

        t1.tree.addTreeSelectionListener(selectionListener);
        t1.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        gpa.load(1,4, t1).add();

        //--------------------------------------------------------------------------------------------------------------

        l3.setText("Resolutions");
        gpb.load(0,0, l3).removeScaleY().add();

        Action removeRow = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                int row = Integer.parseInt(e.getActionCommand());
                Property property = (Property) t2.getModel().getValueAt(row,0);
                property.setSolved(false);
                t0.revalidate();
                t0.repaint();
            }
        };

        t2.buildTable(new String[]{"Property", "Resolution", ""}, removeRow);
        properties.stream().filter(priorProperties::contains).forEach(p -> {
            p.setSolved(true);
            Resolution resolution = Resolution.linkResolution(p.key.toString());
            t2.addRow(new Object[]{p, resolution, "X"});
        });
        t0.revalidate();
        t0.repaint();

        JScrollPane s0 = new JScrollPane(t2);
        gpb.load(0,1, s0).add();

        //--------------------------------------------------------------------------------------------------------------
        gp0.load(0,0, gpa).setWeight(0.6f,1).add();

        gp0.load(1,0, gpb).setWeight(0.4f,1).setLeftPad(20).add();

        add(gp0,BorderLayout.CENTER);
    }

    private void buildRow(JSimpleTable table, Property property, Resolution resolution) {
        Object[] row = new Object[3];
        row[0] = property;
        row[1] = resolution;
        row[2] = "X";
        table.addRow(row);
    }
}

class JMessageTable extends JTable {

    public JMessageTable(String[][] data, String[] columnNames) {
        DefaultTableModel model = new DefaultTableModel(data,columnNames);
        setModel(model);

        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setResizingAllowed(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setCellSelectionEnabled(true);
        setFocusable(true);
        setShowGrid(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        setDefaultRenderer(Object.class, centerRenderer);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment( SwingConstants.LEFT );
        getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        JLabel comp = (JLabel)super.prepareRenderer(renderer, row, col);
        String value = (String) getModel().getValueAt(row, col);
        if(col>1) {
            if (value != null) {
                comp.setText("Edit");
                comp.setBackground(new Color(60, 63, 65));
                if (isCellSelected(row, col))
                    comp.setBorder(BorderFactory.createLoweredBevelBorder());
                else
                    comp.setBorder(BorderFactory.createRaisedBevelBorder());
            } else {
                comp.setBackground(new Color(43, 43, 43));
            }
        }
        return comp;
    }

    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        return false;
    }

    @Override
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        if (columnIndex == 0 || columnIndex == 1 || getValueAt(rowIndex,columnIndex) == null) {
           return;
        }
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
    }
}

class JTreePanel extends JPanel {

    public JTree tree;

    public JTreePanel(Set<Property> properties, boolean colorNodes) {
        setLayout(new BorderLayout());
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        createNodes(root, properties);
        tree = new JTree(root);
        tree.setDragEnabled(false);
        expandAllNodes(tree, 0, tree.getRowCount());
        tree.setRootVisible(false);
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                          boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
                if (userObject instanceof Property) {
                    Property property = (Property) userObject;
                    this.setText(((Property) userObject).key.name);
                    if(colorNodes) {
                        if(property.solved) {
                            this.setForeground(new Color(73, 156, 84));
                        }else {
                            this.setForeground(new Color(199, 84, 80));
                        }
                    }
                } else {
                    this.setText((String) userObject);
                }
                return this;
            }
        });
        tree.setSelectionModel(new DefaultTreeSelectionModel() {
            @Override
            public void setSelectionPath(TreePath path) {
                if(Optional.ofNullable(path.getLastPathComponent()).map(p -> ((DefaultMutableTreeNode) p).getUserObject()).filter(p0 -> p0 instanceof Property).orElse(null) == null) {
                    return;
                }

                if(tree.isPathSelected(path)) {
                    tree.removeSelectionPath(path);
                }
                else {
                    super.setSelectionPath(path);
                }
            }
        });
        JScrollPane s0 = new JScrollPane(tree);
        add(s0,BorderLayout.CENTER);
    }

    private void expandAllNodes(JTree tree, int startingIndex, int rowCount){
        for(int i=startingIndex;i<rowCount;++i){
            tree.expandRow(i);
        }

        if(tree.getRowCount()!=rowCount){
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    private void createNodes(DefaultMutableTreeNode root, Set<Property> properties) {
        Map<String, Set<Property>> categories = new HashMap<>();

        for(PropertyKey.Location location : PropertyKey.Location.values()) {
            categories.put(location.name().toLowerCase(), new HashSet<>());
        }

        for(Property property : properties) {
            for (Map.Entry<String, Set<Property>> category : categories.entrySet()) {
                if(property.key.location.name().toLowerCase().equals(category.getKey())) {
                    category.getValue().add(property);
                    break;
                }
            }
        }

        for(Map.Entry<String, Set<Property>> category : categories.entrySet()) {
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category.getKey());
            root.add(categoryNode);
            createNodes(categoryNode, category.getValue(), 0);
        }
    }

    private void createNodes(DefaultMutableTreeNode parent, Set<Property> properties, int depth) {
        Map<String, Set<Property>> children = new HashMap<>();
        for(Property property : properties) {
            if(property.key.predecessors.size() == depth) {
                DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(property);
                parent.add(categoryNode);
            }
            else {
                String parentName = property.key.predecessors.get(depth);
                children.putIfAbsent(parentName, new HashSet<>());
                children.get(parentName).add(property);
            }
        }

        for(Map.Entry<String,Set<Property>> entry : children.entrySet()) {
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(entry.getKey());
            parent.add(categoryNode);
            createNodes(categoryNode, entry.getValue(), depth+1);
        }
    }
}

class JValuePanel extends JPanel {

    JTextField value;

    public JValuePanel() {
        setLayout(new BorderLayout());
        JGridBagPanel gp0 = new JGridBagPanel();

        JLabel valueLabel = new JLabel("Property Value: ");
        value = new JTextField("");

        gp0.load(0,0,valueLabel).removeScaleY().setTopPad(10).setBottomPad(10).setLeftPad(10).add();
        gp0.load(1,0,value).removeScaleY().setTopPad(10).setBottomPad(10).setRightPad(10).add();

        add(gp0,BorderLayout.PAGE_START);
        setBorder(UIManager.getBorder("ScrollPane.border"));
    }

    public void setValue(String v) {
        value.setText(v);
    }
}

class JPropertyPanel extends JPanel {

    JTextField name;
    JCheckBox isArray;
    JTextField primitive;
    JTextField format;

    public JPropertyPanel() {
        setLayout(new BorderLayout());
        JGridBagPanel gp0 = new JGridBagPanel();

        JLabel nameLabel = new JLabel("Property Name: ");
        name = new JTextField("");
        name.setEnabled(false);

        gp0.load(0,0,nameLabel).setTopPad(10).setLeftPad(10).add();
        gp0.load(1,0,name).setTopPad(10).setRightPad(10).add();

        JLabel arrayLabel = new JLabel("Is Array: ");
        isArray = new JCheckBox("", false);
        isArray.setEnabled(false);

        gp0.load(0,1,arrayLabel).setLeftPad(10).add();
        gp0.load(1,1,isArray).setRightPad(10).add();

        JLabel primitiveLabel = new JLabel("Type: ");
        primitive = new JTextField("");
        primitive.setEnabled(false);

        gp0.load(0,2,primitiveLabel).setLeftPad(10).add();
        gp0.load(1,2,primitive).setRightPad(10).add();

        JLabel formatLabel = new JLabel("Format: ");
        format = new JTextField("");
        format.setEnabled(false);

        gp0.load(0,3,formatLabel).setLeftPad(10).setBottomPad(15).add();
        gp0.load(1,3,format).setRightPad(10).setBottomPad(10).add();

        add(gp0,BorderLayout.CENTER);

        setBorder(UIManager.getBorder("ScrollPane.border"));
    }

    public void viewProperty(Property property) {
        name.setText(property.key.name);
        isArray.setSelected(property.array);
        primitive.setText(property.primitive);
        format.setText(property.format);
    }

    public void resetView() {
        name.setText("");
        isArray.setSelected(false);
        primitive.setText("");
        format.setText("");
    }
}

class JSimpleTable extends JTable {

    private final DefaultTableModel model;

    private final DefaultTableCellRenderer propertyColumnCellRenderer;
    private final DefaultTableCellRenderer resolutionColumnCellRenderer;

    public JSimpleTable() {
        super();
        model = new DefaultTableModel();

        propertyColumnCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setText(((Property)value).key.toString());
                return l;
            }
        };
        resolutionColumnCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setText(((Resolution)value).resolution);
                return l;
            }
        };
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == model.getColumnCount()-1;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        if(column == 0) {
            return propertyColumnCellRenderer;
        }
        else if(column == 1) {
            return resolutionColumnCellRenderer;
        }
        return super.getCellRenderer(row,column);
    }

    public void buildTable(String[] columnNames, Action additionalRemoveRowAction) {
        model.setColumnIdentifiers(columnNames);
        setModel(model);

        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setResizingAllowed(false);
        setCellSelectionEnabled(false);
        setFocusable(false);
        setShowGrid(false);

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

    public void addRow(Object[] row) {
        model.addRow(row);
    }

    public Object[][] getValues() {
        Object[][] values = new Object[model.getRowCount()][model.getColumnCount()-1];
        for(int i=0; i<model.getRowCount(); i++) {
            values[i][0] = getValueAt(i,0);
            values[i][1] = getValueAt(i,1);
        }
        return values;
    }
}
