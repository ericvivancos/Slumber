using System;
using System.Runtime.InteropServices;
using System.Windows;
using System.Windows.Threading;
using Slumber.Agent.Windows.Services;


namespace Slumber.Agent.Windows
{
    public partial class MainWindow : Window
    {
        [DllImport("user32.dll")]
        private static extern void keybd_event(byte bVk, byte bScan, int dwFlags, int dwExtraInfo);

        private const int KEYEVENTF_EXTENDEDKEY = 0x1;
        private const int KEYEVENTF_KEYUP = 0x2;
        private const byte VK_MEDIA_PAUSE = 0xB2;

        private readonly InactivityService _inactivityService;
        private readonly DispatcherTimer _monitoringTimer;
        private readonly MediaSessionService _mediaSessionService = new MediaSessionService();

        private readonly TimeSpan _idleThreshold = TimeSpan.FromSeconds(20); // luego lo subimos
        //private readonly TimeSpan _promptCooldown = TimeSpan.FromMinutes(5);
        private readonly TimeSpan _promptCooldown = TimeSpan.FromSeconds(15);

        private bool _isOverlayOpen = false;
        private DateTime _lastPromptDismissedAt = DateTime.MinValue;

        public MainWindow()
        {
            InitializeComponent();

            _inactivityService = new InactivityService();

            _monitoringTimer = new DispatcherTimer
            {
                Interval = TimeSpan.FromSeconds(2)
            };

            _monitoringTimer.Tick += MonitoringTimer_Tick;
            _monitoringTimer.Start();
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

            // 👇 NUEVO
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

            var overlay = new OverlayWindow();
            overlay.ShowDialog();

            if (overlay.ShouldPause)
            {
                PausePlayback();
            }
            else
            {
                _lastPromptDismissedAt = DateTime.Now; // 👈 ESTO ES CLAVE
            }

            _isOverlayOpen = false;
        }

        private void PausePlayback()
        {
            keybd_event(VK_MEDIA_PAUSE, 0, KEYEVENTF_EXTENDEDKEY, 0);
            keybd_event(VK_MEDIA_PAUSE, 0, KEYEVENTF_EXTENDEDKEY | KEYEVENTF_KEYUP, 0);
        }
    }
}