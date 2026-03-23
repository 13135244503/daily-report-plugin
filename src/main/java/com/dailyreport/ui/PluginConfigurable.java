package com.dailyreport.ui;

import com.dailyreport.config.PluginGlobalConfig;
import com.dailyreport.config.ProjectConfig;
import com.dailyreport.component.PluginConfigComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 插件配置界面
 */
public class PluginConfigurable implements Configurable {
    
    private JPanel mainPanel;
    
    // 项目配置相关组件
    private JPanel projectConfigPanel;
    private DefaultListModel<ProjectConfig> projectListModel;
    private JList<ProjectConfig> projectList;
    private JButton addButton;
    private JButton removeButton;
    private JButton enableButton;
    private JButton disableButton;
    
    // 输出配置相关组件
    private TextFieldWithBrowseButton outputRootPathField;
    
    // AI 配置相关组件
    private JComboBox<String> protocolComboBox;
    private JTextField baseUrlField;
    private JPasswordField apiKeyField;
    private JTextField modelField;
    private JTextField timeoutField;
    private JButton testConnectionButton;
    
    // 翻译配置相关组件
    private JComboBox<String> translationProviderComboBox;
    private JTextField hotkeyField;
    private JCheckBox showVariableNameCheckBox;
    private JCheckBox showConstantNameCheckBox;
    private JCheckBox showCamelCaseCheckBox;
    private JCheckBox showMethodNameCheckBox;
    private JCheckBox showClassNameCheckBox;
    private JCheckBox showFileNameCheckBox;
    private JTextField baiduAppIdField;
    private JPasswordField baiduKeyField;
    private JTextField youdaoAppKeyField;
    private JPasswordField youdaoAppSecretField;
    private JButton testTranslationButton;
    
    private PluginGlobalConfig config;
    private boolean modified = false;
    
    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "日报周报插件";
    }
    
    @Override
    public @Nullable JComponent createComponent() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new TitledBorder("日报周报插件配置"));
        
        // 创建配置标签页
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // ① 项目配置子面板
        projectConfigPanel = createProjectConfigPanel();
        tabbedPane.addTab("项目配置", projectConfigPanel);
        
        // ② 输出配置子面板
        JPanel outputPanel = createOutputPanel();
        tabbedPane.addTab("输出配置", outputPanel);
        
        // ③ AI 配置子面板
        JPanel aiPanel = createAIConfigPanel();
        tabbedPane.addTab("AI 配置", aiPanel);
        
        // ④ 翻译配置子面板
        JPanel translationPanel = createTranslationConfigPanel();
        tabbedPane.addTab("翻译配置", translationPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // 添加恢复默认配置按钮
        JButton resetButton = new JButton("恢复默认配置");
        resetButton.addActionListener(e -> resetToDefault());
        mainPanel.add(resetButton, BorderLayout.SOUTH);
        
        // 加载配置
        loadConfigToUI();
        
        return mainPanel;
    }
    
    /**
     * 创建项目配置面板
     */
    private JPanel createProjectConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // 项目列表
        projectListModel = new DefaultListModel<>();
        projectList = new JList<>(projectListModel);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(projectList);
        
        // 操作按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("添加项目");
        removeButton = new JButton("删除项目");
        enableButton = new JButton("启用");
        disableButton = new JButton("禁用");
        
        addButton.addActionListener(e -> {
            addProject();
            modified = true;
        });
        removeButton.addActionListener(e -> {
            removeProject();
            modified = true;
        });
        enableButton.addActionListener(e -> {
            setProjectEnabled(true);
            modified = true;
        });
        disableButton.addActionListener(e -> {
            setProjectEnabled(false);
            modified = true;
        });
        
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(enableButton);
        buttonPanel.add(disableButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 创建输出配置面板
     */
    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel label = new JLabel("报告输出根路径:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(label, gbc);
        
        outputRootPathField = new TextFieldWithBrowseButton();
        outputRootPathField.addBrowseFolderListener(
            "选择输出目录",
            "请选择报告输出的根目录",
            null,
            new com.intellij.openapi.fileChooser.FileChooserDescriptor(false, true, false, false, false, false)
        );
        outputRootPathField.getTextField().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                modified = true;
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(outputRootPathField, gbc);
        
        return panel;
    }
    
    /**
     * 创建 AI 配置面板（重点适配 Coding Plan 接口）
     */
    private JPanel createAIConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 协议选择
        JLabel protocolLabel = new JLabel("接口协议:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(protocolLabel, gbc);
        
        protocolComboBox = new JComboBox<>(new String[]{"OpenAI", "Anthropic", "HuaweiCloud"});
        protocolComboBox.setSelectedItem("HuaweiCloud");
        protocolComboBox.addActionListener(e -> {
            onProtocolChanged();
            modified = true;
        });
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(protocolComboBox, gbc);
        
        // Base URL
        JLabel baseUrlLabel = new JLabel("Base URL:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(baseUrlLabel, gbc);
        
        baseUrlField = new JTextField(40);
        baseUrlField.setText("https://qianfan.baidubce.com/v2/coding/openai-completions");
        baseUrlField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                modified = true;
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(baseUrlField, gbc);
        
        // API Key
        JLabel apiKeyLabel = new JLabel("API Key:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(apiKeyLabel, gbc);
        
        apiKeyField = new JPasswordField(40);
        apiKeyField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                modified = true;
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(apiKeyField, gbc);
        
        // Model
        JLabel modelLabel = new JLabel("模型名称:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        panel.add(modelLabel, gbc);
        
        modelField = new JTextField(40);
        modelField.setText("qwen3-30b-a3b");
        modelField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                modified = true;
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(modelField, gbc);
        
        // 超时时间
        JLabel timeoutLabel = new JLabel("请求超时 (秒):");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        panel.add(timeoutLabel, gbc);
        
        timeoutField = new JTextField(10);
        timeoutField.setText("30");
        timeoutField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                modified = true;
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(timeoutField, gbc);
        
        // 测试连接按钮
        testConnectionButton = new JButton("测试连接");
        testConnectionButton.addActionListener(e -> testConnection());
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(testConnectionButton, gbc);
        
        return panel;
    }
    
    /**
     * 协议改变时的处理
     */
    private void onProtocolChanged() {
        String protocol = (String) protocolComboBox.getSelectedItem();
        if ("OpenAI".equals(protocol)) {
            baseUrlField.setText("https://qianfan.baidubce.com/v2/coding/openai-completions");
        } else if ("Anthropic".equals(protocol)) {
            baseUrlField.setText("https://qianfan.baidubce.com/anthropic/coding/v1/messages");} else if ("HuaweiCloud".equals(protocol)) {
            baseUrlField.setText("https://api.modelarts-maas.com/v2/chat/completions");
        }
    }
    
    /**
     * 测试 AI 连接
     */
    private void testConnection() {
        // 先保存当前配置
        saveConfigFromUI();
        
        // 显示测试中提示
        testConnectionButton.setEnabled(false);
        testConnectionButton.setText("测试中...");
        
        // 在后台线程中测试连接
        new Thread(() -> {
            try {
                com.dailyreport.ai.CodingPlanAIAnalyzer aiAnalyzer = 
                    new com.dailyreport.ai.CodingPlanAIAnalyzer(config);
                boolean success = aiAnalyzer.testConnection();
                
                // 在EDT线程中显示结果
                javax.swing.SwingUtilities.invokeLater(() -> {
                    testConnectionButton.setEnabled(true);
                    testConnectionButton.setText("测试连接");
                    
                    if (success) {
                        JOptionPane.showMessageDialog(mainPanel, 
                            "连接成功！", 
                            "测试结果", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(mainPanel, 
                            "连接失败，请检查 Base URL 和 API Key 是否正确。", 
                            "测试结果", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception e) {
                // 在EDT线程中显示错误
                javax.swing.SwingUtilities.invokeLater(() -> {
                    testConnectionButton.setEnabled(true);
                    testConnectionButton.setText("测试连接");
                    JOptionPane.showMessageDialog(mainPanel, 
                        "连接失败：" + e.getMessage(), 
                        "测试结果", 
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    /**
     * 创建翻译配置面板
     */
    private JPanel createTranslationConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 翻译服务提供商
        JLabel providerLabel = new JLabel("翻译服务:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(providerLabel, gbc);
        
        translationProviderComboBox = new JComboBox<>(new String[]{"Baidu", "Youdao"});
        translationProviderComboBox.addActionListener(e -> {
            modified = true;
            updateTranslationFields();
        });
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(translationProviderComboBox, gbc);
        
        // 快捷键
        JLabel hotkeyLabel = new JLabel("快捷键:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(hotkeyLabel, gbc);
        
        hotkeyField = new JTextField(20);
        hotkeyField.setText("ctrl shift T");
        hotkeyField.setEditable(false);
        hotkeyField.setFocusable(true);
        hotkeyField.setToolTipText("点击后按下快捷键组合（如 Ctrl+Shift+Y）");
        
        hotkeyField.addKeyListener(new java.awt.event.KeyAdapter() {
            private java.util.List<Integer> pressedKeys = new java.util.ArrayList<>();
            
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (!pressedKeys.contains(e.getKeyCode())) {
                    pressedKeys.add(e.getKeyCode());
                }
            }
            
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (pressedKeys.contains(e.getKeyCode())) {
                    pressedKeys.remove(Integer.valueOf(e.getKeyCode()));
                }
                
                if (pressedKeys.isEmpty()) {
                    String hotkeyText = buildHotkeyText(e);
                    if (!hotkeyText.isEmpty()) {
                        hotkeyField.setText(hotkeyText);
                        modified = true;
                    }
                }
            }
            
            private String buildHotkeyText(java.awt.event.KeyEvent e) {
                StringBuilder sb = new StringBuilder();
                java.util.List<String> modifiers = new java.util.ArrayList<>();
                
                if (e.isControlDown()) modifiers.add("ctrl");
                if (e.isShiftDown()) modifiers.add("shift");
                if (e.isAltDown()) modifiers.add("alt");
                if (e.isMetaDown()) modifiers.add("meta");
                
                for (String modifier : modifiers) {
                    sb.append(modifier).append(" ");
                }
                
                String keyText = java.awt.event.KeyEvent.getKeyText(e.getKeyCode());
                if (!modifiers.isEmpty()) {
                    sb.append(keyText);
                }
                
                return sb.toString().trim();
            }
        });
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(hotkeyField, gbc);
        
        // 分隔线
        JSeparator separator = new JSeparator();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(separator, gbc);
        
        // 显示选项
        JLabel showOptionsLabel = new JLabel("显示选项:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        panel.add(showOptionsLabel, gbc);
        
        showVariableNameCheckBox = new JCheckBox("变量名 (snake_case)", true);
        showVariableNameCheckBox.addActionListener(e -> modified = true);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(showVariableNameCheckBox, gbc);
        
        showConstantNameCheckBox = new JCheckBox("常量名 (UPPER_SNAKE_CASE)", true);
        showConstantNameCheckBox.addActionListener(e -> modified = true);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(showConstantNameCheckBox, gbc);
        
        showCamelCaseCheckBox = new JCheckBox("驼峰命名 (camelCase)", true);
        showCamelCaseCheckBox.addActionListener(e -> modified = true);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(showCamelCaseCheckBox, gbc);
        
        showMethodNameCheckBox = new JCheckBox("方法名 (methodName)", true);
        showMethodNameCheckBox.addActionListener(e -> modified = true);
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        panel.add(showMethodNameCheckBox, gbc);
        
        showClassNameCheckBox = new JCheckBox("类名 (ClassName)", true);
        showClassNameCheckBox.addActionListener(e -> modified = true);
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        panel.add(showClassNameCheckBox, gbc);
        
        showFileNameCheckBox = new JCheckBox("文件名 (file_name)", true);
        showFileNameCheckBox.addActionListener(e -> modified = true);
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        panel.add(showFileNameCheckBox, gbc);
        
        // 分隔线
        JSeparator separator2 = new JSeparator();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(separator2, gbc);
        
        // 百度翻译配置
        JLabel baiduLabel = new JLabel("百度翻译配置:");
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        panel.add(baiduLabel, gbc);
        
        JLabel baiduAppIdLabel = new JLabel("App ID:");
        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.weightx = 0;
        panel.add(baiduAppIdLabel, gbc);
        
        baiduAppIdField = new JTextField(30);
        baiduAppIdField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                modified = true;
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(baiduAppIdField, gbc);
        
        JLabel baiduKeyLabel = new JLabel("密钥:");
        gbc.gridx = 0;
        gbc.gridy = 13;
        gbc.weightx = 0;
        panel.add(baiduKeyLabel, gbc);
        
        baiduKeyField = new JPasswordField(30);
        baiduKeyField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                modified = true;
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(baiduKeyField, gbc);
        
        // 有道翻译配置
        JLabel youdaoLabel = new JLabel("有道翻译配置:");
        gbc.gridx = 0;
        gbc.gridy = 14;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        panel.add(youdaoLabel, gbc);
        
        JLabel youdaoAppKeyLabel = new JLabel("App Key:");
        gbc.gridx = 0;
        gbc.gridy = 15;
        gbc.weightx = 0;
        panel.add(youdaoAppKeyLabel, gbc);
        
        youdaoAppKeyField = new JTextField(30);
        youdaoAppKeyField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                modified = true;
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(youdaoAppKeyField, gbc);
        
        JLabel youdaoAppSecretLabel = new JLabel("App Secret:");
        gbc.gridx = 0;
        gbc.gridy = 16;
        gbc.weightx = 0;
        panel.add(youdaoAppSecretLabel, gbc);
        
        youdaoAppSecretField = new JPasswordField(30);
        youdaoAppSecretField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                modified = true;
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(youdaoAppSecretField, gbc);
        
        // 测试翻译按钮
        JButton testTranslationButton = new JButton("测试翻译");
        testTranslationButton.addActionListener(e -> testTranslation());
        gbc.gridx = 0;
        gbc.gridy = 17;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(testTranslationButton, gbc);
        
        updateTranslationFields();
        
        return panel;
    }
    
    /**
     * 根据选择的翻译服务更新字段显示
     */
    private void updateTranslationFields() {
        String provider = (String) translationProviderComboBox.getSelectedItem();
        boolean isBaidu = "Baidu".equals(provider);
        
        baiduAppIdField.setEnabled(isBaidu);
        baiduKeyField.setEnabled(isBaidu);
        youdaoAppKeyField.setEnabled(!isBaidu);
        youdaoAppSecretField.setEnabled(!isBaidu);
    }
    
    /**
     * 测试翻译功能
     */
    private void testTranslation() {
        // 先保存当前配置
        saveConfigFromUI();
        
        // 显示测试中提示
        testTranslationButton.setEnabled(false);
        testTranslationButton.setText("测试中...");
        
        // 在后台线程中测试翻译
        new Thread(() -> {
            try {
                com.dailyreport.translation.TranslationService translationService = 
                    new com.dailyreport.translation.TranslationService(config);
                
                com.dailyreport.translation.TranslationService.TranslationResult result = 
                    translationService.translate("测试");
                
                // 在EDT线程中显示结果
                javax.swing.SwingUtilities.invokeLater(() -> {
                    testTranslationButton.setEnabled(true);
                    testTranslationButton.setText("测试翻译");
                    
                    JOptionPane.showMessageDialog(mainPanel, 
                        "翻译测试成功！\n\n" +
                        "测试文本: 测试\n" +
                        "翻译结果: " + result.getEnglishText() + "\n\n" +
                        "变量名: " + result.toVariableName() + "\n" +
                        "驼峰命名: " + result.toCamelCase(), 
                        "测试结果", 
                        JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception e) {
                // 在EDT线程中显示错误
                javax.swing.SwingUtilities.invokeLater(() -> {
                    testTranslationButton.setEnabled(true);
                    testTranslationButton.setText("测试翻译");
                    JOptionPane.showMessageDialog(mainPanel, 
                        "翻译测试失败：" + e.getMessage(), 
                        "测试结果", 
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    /**
     * 添加项目
     */
    private void addProject() {
        // 创建项目选择对话框
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("选择项目目录");
        
        int result = fileChooser.showOpenDialog(mainPanel);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String projectPath = selectedFile.getAbsolutePath();
            
            // 检查是否已存在
            for (int i = 0; i < projectListModel.size(); i++) {
                ProjectConfig existingProject = projectListModel.getElementAt(i);
                if (existingProject.getPath().equals(projectPath)) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "该项目已存在！", 
                        "提示", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            
            // 创建新项目配置
            ProjectConfig newProject = new ProjectConfig();
            newProject.setPath(projectPath);
            newProject.setEnabled(true);
            
            // 添加到列表
            projectListModel.addElement(newProject);
            
            // 选中新增的项目
            projectList.setSelectedIndex(projectListModel.size() - 1);
        }
    }
    
    /**
     * 删除项目
     */
    private void removeProject() {
        int selectedIndex = projectList.getSelectedIndex();
        if (selectedIndex != -1) {
            projectListModel.remove(selectedIndex);
        }
    }
    
    /**
     * 设置项目启用状态
     */
    private void setProjectEnabled(boolean enabled) {
        int selectedIndex = projectList.getSelectedIndex();
        if (selectedIndex != -1) {
            ProjectConfig config = projectListModel.getElementAt(selectedIndex);
            config.setEnabled(enabled);
            projectList.repaint();
        }
    }
    
    /**
     * 恢复默认配置
     */
    private void resetToDefault() {
        int result = JOptionPane.showConfirmDialog(mainPanel, 
            "确定要恢复默认配置吗？", 
            "确认", 
            JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            config = new PluginGlobalConfig();
            loadConfigToUI();
            modified = true;
        }
    }
    
    /**
     * 加载配置到 UI
     */
    private void loadConfigToUI() {
        // 从持久化组件获取配置
        config = PluginConfigComponent.getInstance();
        
        // 加载项目列表
        projectListModel.clear();
        for (ProjectConfig project : config.getProjects()) {
            projectListModel.addElement(project);
        }
        
        // 加载输出路径
        outputRootPathField.setText(config.getOutputRootPath());
        
        // 加载 AI 配置
        PluginGlobalConfig.AIConfig aiConfig = config.getAiConfig();
        protocolComboBox.setSelectedItem(aiConfig.getProtocol());
        baseUrlField.setText(aiConfig.getBaseUrl());
        apiKeyField.setText(aiConfig.getApiKey() != null ? aiConfig.getApiKey() : "");
        modelField.setText(aiConfig.getModel() != null ? aiConfig.getModel() : "");
        timeoutField.setText(String.valueOf(aiConfig.getTimeout()));
        
        // 加载翻译配置
        PluginGlobalConfig.TranslationConfig translationConfig = config.getTranslationConfig();
        translationProviderComboBox.setSelectedItem(translationConfig.getProvider());
        hotkeyField.setText(translationConfig.getHotkey());
        showVariableNameCheckBox.setSelected(translationConfig.isShowVariableName());
        showConstantNameCheckBox.setSelected(translationConfig.isShowConstantName());
        showCamelCaseCheckBox.setSelected(translationConfig.isShowCamelCase());
        showMethodNameCheckBox.setSelected(translationConfig.isShowMethodName());
        showClassNameCheckBox.setSelected(translationConfig.isShowClassName());
        showFileNameCheckBox.setSelected(translationConfig.isShowFileName());
        baiduAppIdField.setText(translationConfig.getBaiduAppId() != null ? translationConfig.getBaiduAppId() : "");
        baiduKeyField.setText(translationConfig.getBaiduKey() != null ? translationConfig.getBaiduKey() : "");
        youdaoAppKeyField.setText(translationConfig.getYoudaoAppKey() != null ? translationConfig.getYoudaoAppKey() : "");
        youdaoAppSecretField.setText(translationConfig.getYoudaoAppSecret() != null ? translationConfig.getYoudaoAppSecret() : "");
        
        // 重置修改标志
        modified = false;
    }
    
    /**
     * 从 UI 保存配置
     */
    private void saveConfigFromUI() {
        if (config == null) {
            config = new PluginGlobalConfig();
        }
        
        // 保存项目列表
        List<ProjectConfig> projects = new ArrayList<>();
        for (int i = 0; i < projectListModel.size(); i++) {
            projects.add(projectListModel.getElementAt(i));
        }
        config.setProjects(projects);
        
        // 保存输出路径
        config.setOutputRootPath(outputRootPathField.getText());
        
        // 保存 AI 配置
        PluginGlobalConfig.AIConfig aiConfig = config.getAiConfig();
        aiConfig.setProtocol((String) protocolComboBox.getSelectedItem());
        aiConfig.setBaseUrl(baseUrlField.getText());
        aiConfig.setApiKey(new String(apiKeyField.getPassword()));
        aiConfig.setModel(modelField.getText());
        try {
            aiConfig.setTimeout(Integer.parseInt(timeoutField.getText()));
        } catch (NumberFormatException e) {
            aiConfig.setTimeout(30);
        }
        
        // 保存翻译配置
        PluginGlobalConfig.TranslationConfig translationConfig = config.getTranslationConfig();
        translationConfig.setProvider((String) translationProviderComboBox.getSelectedItem());
        translationConfig.setHotkey(hotkeyField.getText());
        translationConfig.setShowVariableName(showVariableNameCheckBox.isSelected());
        translationConfig.setShowConstantName(showConstantNameCheckBox.isSelected());
        translationConfig.setShowCamelCase(showCamelCaseCheckBox.isSelected());
        translationConfig.setShowMethodName(showMethodNameCheckBox.isSelected());
        translationConfig.setShowClassName(showClassNameCheckBox.isSelected());
        translationConfig.setShowFileName(showFileNameCheckBox.isSelected());
        translationConfig.setBaiduAppId(baiduAppIdField.getText());
        translationConfig.setBaiduKey(new String(baiduKeyField.getPassword()));
        translationConfig.setYoudaoAppKey(youdaoAppKeyField.getText());
        translationConfig.setYoudaoAppSecret(new String(youdaoAppSecretField.getPassword()));
        
        // 持久化配置
        PluginConfigComponent.saveConfig(config);
        
        // 重置修改标志
        modified = false;
    }
    
    @Override
    public boolean isModified() {
        return modified;
    }
    
    @Override
    public void apply() {
        saveConfigFromUI();
    }
}