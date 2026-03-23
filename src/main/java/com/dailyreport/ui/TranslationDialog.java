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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        setSize(600, 500);
        init();
    }
    
    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel inputLabel = new JLabel("输入中文（按回车翻译）:");
        inputField = new JBTextField();
        inputField.setPreferredSize(new Dimension(0, 30));
        inputField.addActionListener(this::onTranslate);
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(inputField, BorderLayout.CENTER);
        
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBorder(BorderFactory.createTitledBorder("翻译结果（点击复制）"));
        
        JScrollPane scrollPane = new JScrollPane(resultPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
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
            
            if (config.isShowVariableName()) {
                addResultItem("变量名 (snake_case)", result.toVariableName());
            }
            
            if (config.isShowConstantName()) {
                addResultItem("常量名 (UPPER_SNAKE_CASE)", result.toConstantName());
            }
            
            if (config.isShowCamelCase()) {
                addResultItem("驼峰命名 (camelCase)", result.toCamelCase());
            }
            
            if (config.isShowMethodName()) {
                addResultItem("方法名 (methodName)", result.toMethodName());
            }
            
            if (config.isShowClassName()) {
                addResultItem("类名 (ClassName)", result.toClassName());
            }
            
            if (config.isShowFileName()) {
                addResultItem("文件名 (file_name)", result.toFileName());
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
    
    private void addResultItem(String label, String value) {
        JPanel itemPanel = new JPanel(new BorderLayout(5, 5));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JLabel typeLabel = new JLabel(label);
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.BOLD));
        
        JTextField valueField = new JTextField(value);
        valueField.setEditable(false);
        valueField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                copyToClipboard(value);
                valueField.setBackground(new Color(200, 255, 200));
                Timer timer = new Timer(200, ev -> {
                    valueField.setBackground(Color.WHITE);
                    ((Timer) ev.getSource()).stop();
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
        
        itemPanel.add(typeLabel, BorderLayout.NORTH);
        itemPanel.add(valueField, BorderLayout.CENTER);
        
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
}