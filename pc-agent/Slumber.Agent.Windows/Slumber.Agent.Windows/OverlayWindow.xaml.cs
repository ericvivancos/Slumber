using System;
using System.Windows;
using System.Windows.Threading;
using Slumber.Agent.Windows.Services;

namespace Slumber.Agent.Windows
{
    public partial class OverlayWindow : Window
    {
        private readonly DispatcherTimer _countdownTimer;
        private readonly GlobalInputHookService _globalInputHookService;

        private int _secondsRemaining = 15;

        public bool ShouldPause { get; private set; } = false;

        public OverlayWindow()
        {
            InitializeComponent();

            _countdownTimer = new DispatcherTimer
            {
                Interval = TimeSpan.FromSeconds(1)
            };
            _countdownTimer.Tick += CountdownTimer_Tick;

            _globalInputHookService = new GlobalInputHookService();
            _globalInputHookService.UserActivityDetected += GlobalInputHookService_UserActivityDetected;

            Loaded += OverlayWindow_Loaded;
            Closed += OverlayWindow_Closed;
        }

        private void OverlayWindow_Loaded(object sender, RoutedEventArgs e)
        {
            CountdownText.Text = $"Pausando en {_secondsRemaining} segundos...";
            _countdownTimer.Start();
            _globalInputHookService.Start();
        }

        private void OverlayWindow_Closed(object? sender, EventArgs e)
        {
            _globalInputHookService.UserActivityDetected -= GlobalInputHookService_UserActivityDetected;
            _globalInputHookService.Dispose();
        }

        private void CountdownTimer_Tick(object? sender, EventArgs e)
        {
            _secondsRemaining--;

            if (_secondsRemaining <= 0)
            {
                _countdownTimer.Stop();
                ShouldPause = true;
                Close();
                return;
            }

            CountdownText.Text = $"Pausando en {_secondsRemaining} segundos...";
        }

        private void GlobalInputHookService_UserActivityDetected(object? sender, EventArgs e)
        {
            Dispatcher.Invoke(() =>
            {
                CancelOverlay();
            });
        }

        private void ContinueButton_Click(object sender, RoutedEventArgs e)
        {
            CancelOverlay();
        }

        private void CancelOverlay()
        {
            if (!IsVisible)
            {
                return;
            }

            _countdownTimer.Stop();
            ShouldPause = false;
            Close();
        }
    }
}