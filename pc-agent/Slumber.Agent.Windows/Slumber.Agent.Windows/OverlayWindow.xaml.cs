using System;
using System.Windows;
using System.Windows.Threading;

namespace Slumber.Agent.Windows
{
    public partial class OverlayWindow : Window
    {
        private readonly DispatcherTimer _timer;
        private int _secondsRemaining = 15;

        public bool ShouldPause { get; private set; } = false;

        public OverlayWindow()
        {
            InitializeComponent();

            _timer = new DispatcherTimer
            {
                Interval = TimeSpan.FromSeconds(1)
            };

            _timer.Tick += Timer_Tick;
            Loaded += OverlayWindow_Loaded;
        }

        private void OverlayWindow_Loaded(object sender, RoutedEventArgs e)
        {
            CountdownText.Text = $"Pausando en {_secondsRemaining} segundos...";
            _timer.Start();
        }

        private void Timer_Tick(object? sender, EventArgs e)
        {
            _secondsRemaining--;

            if (_secondsRemaining <= 0)
            {
                _timer.Stop();
                ShouldPause = true;
                DialogResult = true;
                Close();
                return;
            }

            CountdownText.Text = $"Pausando en {_secondsRemaining} segundos...";
        }

        private void ContinueButton_Click(object sender, RoutedEventArgs e)
        {
            _timer.Stop();
            ShouldPause = false;
            DialogResult = false;
            Close();
        }
    }
}