using Microsoft.Win32;
using System;
using System.Diagnostics;

namespace Slumber.Agent.Windows.Services
{
    public class StartupService
    {
        private const string RegistryPath = @"Software\Microsoft\Windows\CurrentVersion\Run";
        private const string AppName = "Slumber";

        public bool IsEnabled()
        {
            using var key = Registry.CurrentUser.OpenSubKey(RegistryPath, false);
            var value = key?.GetValue(AppName) as string;

            return !string.IsNullOrWhiteSpace(value);
        }

        public void SetEnabled(bool enabled)
        {
            using var key = Registry.CurrentUser.OpenSubKey(RegistryPath, true);

            if (key == null)
            {
                throw new InvalidOperationException("No se pudo acceder a la clave de inicio de Windows.");
            }

            if (enabled)
            {
                string exePath = Process.GetCurrentProcess().MainModule?.FileName
                                 ?? throw new InvalidOperationException("No se pudo obtener la ruta del ejecutable.");

                key.SetValue(AppName, $"\"{exePath}\"");
            }
            else
            {
                if (key.GetValue(AppName) != null)
                {
                    key.DeleteValue(AppName, false);
                }
            }
        }
    }
}