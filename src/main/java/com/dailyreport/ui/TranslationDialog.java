package com.dailyreport.ui;

import com.dailyreport.config.PluginGlobalConfig;
import com.dailyreport.config.PluginGlobalConfig.TranslationConfig;
import com.dailyreport.component.PluginConfigComponent;
import com.dailyreport.translation.TranslationService;
import com.dailyreport.translation.TranslationService.TranslationResult;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class TranslationDialog extends DialogWrapper {
    
    private static final Logger LOG = Logger.getInstance(TranslationDialog.class);
    
    private JBTextField inputField;
    private JPanel resultPanel;
    private final TranslationConfig config;
    
    public TranslationDialog() {
        super(true);
        PluginGlobalConfig globalConfig = PluginConfigComponent.getInstance();
        this.config = globalConfig.getTranslationConfig();
        setTitle("中文转编程命名");
        setSize(800, 400);
        init();
    }
    
    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel inputLabel = new JLabel("输入中文（按回车翻译）:");
        inputField = new JBTextField();
        inputField.setPreferredSize(new Dimension(0, 35));
        inputField.setFont(inputField.getFont().deriveFont(Font.PLAIN, 14f));
        inputField.addActionListener(this::onTranslate);
        
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(inputField, BorderLayout.CENTER);
        
        resultPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        resultPanel.setBorder(BorderFactory.createTitledBorder("翻译结果（点击复制并关闭）"));
        
        JScrollPane scrollPane = new JScrollPane(resultPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        inputField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "translate");
        inputField.getActionMap().put("translate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onTranslate(e);
            }
        });
        
        return panel;
    }
    
    private void onTranslate(ActionEvent e) {
        String chineseText = inputField.getText().trim();
        if (chineseText.isEmpty()) {
            return;
        }
        
        resultPanel.removeAll();
        
        try {
            TranslationService service = new TranslationService(PluginConfigComponent.getInstance());
            TranslationResult result = service.translate(chineseText);
            
            List<ResultItem> items = new ArrayList<>();
            
            if (config.isShowVariableName()) {
                items.add(new ResultItem("变量名", "snake_case", result.toVariableName()));
            }
            
            if (config.isShowConstantName()) {
                items.add(new ResultItem("常量名", "UPPER_SNAKE_CASE", result.toConstantName()));
            }
            
            if (config.isShowCamelCase()) {
                items.add(new ResultItem("驼峰命名", "camelCase", result.toCamelCase()));
            }
            
            if (config.isShowMethodName()) {
                items.add(new ResultItem("方法名", "methodName", result.toMethodName()));
            }
            
            if (config.isShowClassName()) {
                items.add(new ResultItem("类名", "ClassName", result.toClassName()));
            }
            
            if (config.isShowFileName()) {
                items.add(new ResultItem("文件名", "file_name", result.toFileName()));
            }
            
            for (ResultItem item : items) {
                addResultItem(item);
            }
            
            resultPanel.revalidate();
            resultPanel.repaint();
            
        } catch (Exception ex) {
            LOG.error("翻译失败", ex);
            JOptionPane.showMessageDialog(
                    resultPanel,
                    "翻译失败: " + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private void addResultItem(ResultItem item) {
        JPanel itemPanel = new JPanel(new BorderLayout(5, 5));
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        itemPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(item.typeName);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));
        titleLabel.setForeground(new Color(100, 100, 100));
        
        JLabel formatLabel = new JLabel(item.format);
        formatLabel.setFont(formatLabel.getFont().deriveFont(Font.PLAIN, 10f));
        formatLabel.setForeground(new Color(150, 150, 150));
        
        JLabel valueLabel = new JLabel(item.value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.PLAIN, 14f));
        valueLabel.setForeground(new Color(50, 50, 50));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(formatLabel, BorderLayout.SOUTH);
        
        itemPanel.add(topPanel, BorderLayout.NORTH);
        itemPanel.add(valueLabel, BorderLayout.CENTER);
        
        itemPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                copyToClipboard(item.value);
                close(OK_EXIT_CODE);
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                itemPanel.setBackground(new Color(240, 248, 255));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                itemPanel.setBackground(Color.WHITE);
            }
        });
        
        resultPanel.add(itemPanel);
    }
    
    private void copyToClipboard(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(text);
        clipboard.setContents(selection, null);
    }
    
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return inputField;
    }
    
    private static class ResultItem {
        String typeName;
        String format;
        String value;
        
        ResultItem(String typeName, String format, String value) {
            this.typeName = typeName;
            this.format = format;
            this.value = value;
        }
    }
}