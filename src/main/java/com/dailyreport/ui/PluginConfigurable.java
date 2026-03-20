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