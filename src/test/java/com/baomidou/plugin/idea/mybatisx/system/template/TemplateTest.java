package com.baomidou.plugin.idea.mybatisx.system.template;

import com.baomidou.plugin.idea.mybatisx.generate.dto.ConfigSetting;
import com.baomidou.plugin.idea.mybatisx.generate.setting.DefaultSettingsConfig;
import com.intellij.testFramework.UsefulTestCase;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TemplateTest extends UsefulTestCase {

    public void testDeleteById() throws IOException {

        Map<String, ConfigSetting> map = DefaultSettingsConfig.defaultSettings();
        System.out.println(map);

    }


}
