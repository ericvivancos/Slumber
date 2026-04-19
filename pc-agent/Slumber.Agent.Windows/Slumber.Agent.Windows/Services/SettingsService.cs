using System;
using System.IO;
using System.Text.Json;
using Slumber.Agent.Windows.Models;

namespace Slumber.Agent.Windows.Services
{
    public class SettingsService
    {
        private readonly string _appFolderPath;
        private readonly string _settingsFilePath;
        private readonly JsonSerializerOptions _jsonOptions;

        public SettingsService()
        {
            _appFolderPath = Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
                "Slumber");

            _settingsFilePath = Path.Combine(_appFolderPath, "settings.json");

            _jsonOptions = new JsonSerializerOptions
            {
                WriteIndented = true
            };
        }

        public AppSettings LoadSettings()
        {
            EnsureAppFolderExists();

            if (!File.Exists(_settingsFilePath))
            {
                var defaultSettings = new AppSettings();
                SaveSettings(defaultSettings);
                return defaultSettings;
            }

            try
            {
                var json = File.ReadAllText(_settingsFilePath);
                var settings = JsonSerializer.Deserialize<AppSettings>(json, _jsonOptions);

                return settings ?? new AppSettings();
            }
            catch
            {
                var fallbackSettings = new AppSettings();
                SaveSettings(fallbackSettings);
                return fallbackSettings;
            }
        }

        public void SaveSettings(AppSettings settings)
        {
            EnsureAppFolderExists();

            var json = JsonSerializer.Serialize(settings, _jsonOptions);
            File.WriteAllText(_settingsFilePath, json);
        }

        public string GetSettingsFilePath()
        {
            return _settingsFilePath;
        }

        private void EnsureAppFolderExists()
        {
            if (!Directory.Exists(_appFolderPath))
            {
                Directory.CreateDirectory(_appFolderPath);
            }
        }
    }
}