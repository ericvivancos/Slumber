using Slumber.Agent.Windows.Models;
using Slumber.Agent.Windows.Services;
using System;
using System.ComponentModel;
using System.Drawing;
using System.Runtime.InteropServices;
using System.Windows;
using System.Windows.Threading;
using Forms = System.Windows.Forms;

namespace Slumber.Agent.Windows
{
    public partial class MainWindow : Window
    {
        [DllImport("user32.dll")]
        private static extern void keybd_event(byte bVk, byte bScan, int dwFlags, int dwExtraInfo);

        private const int KEYEVENTF_EXTENDEDKEY = 0x1;
        private const int KEYEVENTF_KEYUP = 0x2;
        private const byte VK_MEDIA_PAUSE = 0xB2;

        private Forms.NotifyIcon? _notifyIcon;

        private readonly InactivityService _inactivityService;
        private readonly DispatcherTimer _monitoringTimer;
        private readonly MediaSessionService _mediaSessionService = new MediaSessionService();

        private readonly SettingsService _settingsService;
        private AppSettings _settings;

        private TimeSpan _idleThreshold;
        private TimeSpan _promptCooldown;

        private bool _isOverlayOpen = false;
        private bool _isExiting = false;
        private DateTime _lastPromptDismissedAt = DateTime.MinValue;

        private void LoadSettingsIntoUi()
        {
            IdleThresholdTextBox.Text = _settings.IdleThresholdSeconds.ToString();
            OverlayCountdownTextBox.Text = _settings.OverlayCountdownSeconds.ToString();
            PromptCooldownTextBox.Text = _settings.PromptCooldownSeconds.ToString();
        }



        public MainWindow()
        {
            InitializeComponent();

            InitializeTrayIcon();
            Loaded += MainWindow_Loaded;

            _settingsService = new SettingsService();
            _settings = _settingsService.LoadSettings();

            _idleThreshold = TimeSpan.FromSeconds(_settings.IdleThresholdSeconds);
            _promptCooldown = TimeSpan.FromSeconds(_settings.PromptCooldownSeconds);

            LoadSettingsIntoUi();

            _inactivityService = new InactivityService();

            _monitoringTimer = new DispatcherTimer
            {
                Interval = TimeSpan.FromSeconds(2)
            };

            _monitoringTimer.Tick += MonitoringTimer_Tick;
            _monitoringTimer.Start();
        }
        private void InitializeTrayIcon()
        {
            _notifyIcon = new Forms.NotifyIcon
            {
                Icon = SystemIcons.Application,
                Visible = true,
                Text = "Slumber - Monitoring"
            };

            var contextMenu = new Forms.ContextMenuStrip();
            contextMenu.Items.Add("Abrir", null, (_, _) => ShowMainWindow());
            contextMenu.Items.Add("Salir", null, (_, _) => ExitApplication());

            _notifyIcon.ContextMenuStrip = contextMenu;
            _notifyIcon.DoubleClick += (_, _) => ShowMainWindow();
        }

        private void MainWindow_Loaded(object sender, RoutedEventArgs e)
        {
            Hide();
        }

        private void ShowMainWindow()
        {
            Show();
            WindowState = WindowState.Normal;
            Activate();
        }

        private void ExitApplication()
        {
            _isExiting = true;

            if (_notifyIcon != null)
            {
                _notifyIcon.Visible = false;
                _notifyIcon.Dispose();
                _notifyIcon = null;
            }

            Application.Current.Shutdown();
        }

        protected override void OnClosing(CancelEventArgs e)
        {
            if (!_isExiting)
            {
                e.Cancel = true;
                Hide();
                return;
            }

            base.OnClosing(e);
        }

        protected override void OnClosed(EventArgs e)
        {
            if (_notifyIcon != null)
            {
                _notifyIcon.Visible = false;
                _notifyIcon.Dispose();
                _notifyIcon = null;
            }

            base.OnClosed(e);
        }

        private void PauseButton_Click(object sender, RoutedEventArgs e)
        {
            PausePlayback();
        }

        private void ShowOverlayButton_Click(object sender, RoutedEventArgs e)
        {
            ShowInactivityOverlay();
        }

        private void MonitoringTimer_Tick(object? sender, EventArgs e)
        {
            if (_isOverlayOpen)
            {
                return;
            }

            if (DateTime.Now - _lastPromptDismissedAt < _promptCooldown)
            {
                return;
            }

            if (!_mediaSessionService.IsAudioPlaying())
            {
                return;
            }

            if (_inactivityService.IsIdleFor(_idleThreshold))
            {
                ShowInactivityOverlay();
            }
        }

        private void ShowInactivityOverlay()
        {
            _isOverlayOpen = true;

            var overlay = new OverlayWindow(_settings.OverlayCountdownSeconds);
            overlay.ShowDialog();

            if (overlay.ShouldPause)
            {
                PausePlayback();
            }
            else
            {
                _lastPromptDismissedAt = DateTime.Now;
            }

            _isOverlayOpen = false;
        }

        private void PausePlayback()
        {
            keybd_event(VK_MEDIA_PAUSE, 0, KEYEVENTF_EXTENDEDKEY, 0);
            keybd_event(VK_MEDIA_PAUSE, 0, KEYEVENTF_EXTENDEDKEY | KEYEVENTF_KEYUP, 0);
        }

        private void SaveSettingsButton_Click(object sender, RoutedEventArgs e)
        {
            if (!int.TryParse(IdleThresholdTextBox.Text, out int idleThresholdSeconds) || idleThresholdSeconds <= 0)
            {
                MessageBox.Show("El tiempo de inactividad antes del aviso debe ser un número mayor que 0.", "Configuración inválida", MessageBoxButton.OK, MessageBoxImage.Warning);
                return;
            }

            if (!int.TryParse(OverlayCountdownTextBox.Text, out int overlayCountdownSeconds) || overlayCountdownSeconds <= 0)
            {
                MessageBox.Show("El tiempo de espera antes de pausar debe ser un número mayor que 0.", "Configuración inválida", MessageBoxButton.OK, MessageBoxImage.Warning);
                return;
            }

            if (!int.TryParse(PromptCooldownTextBox.Text, out int promptCooldownSeconds) || promptCooldownSeconds <= 0)
            {
                MessageBox.Show("El tiempo de espera tras cancelar el aviso debe ser un número mayor que 0.", "Configuración inválida", MessageBoxButton.OK, MessageBoxImage.Warning);
                return;
            }

            _settings.IdleThresholdSeconds = idleThresholdSeconds;
            _settings.OverlayCountdownSeconds = overlayCountdownSeconds;
            _settings.PromptCooldownSeconds = promptCooldownSeconds;

            _settingsService.SaveSettings(_settings);

            _idleThreshold = TimeSpan.FromSeconds(_settings.IdleThresholdSeconds);
            _promptCooldown = TimeSpan.FromSeconds(_settings.PromptCooldownSeconds);

            SettingsStatusTextBlock.Text = "Configuración guardada correctamente.";
        }
    }
}