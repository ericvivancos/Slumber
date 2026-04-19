namespace Slumber.Agent.Windows.Models
{
    public class AppSettings
    {
        /// <summary>
        /// Tiempo de inactividad, en segundos, antes de mostrar el aviso.
        /// </summary>
        public int IdleThresholdSeconds { get; set; } = 300;

        /// <summary>
        /// Tiempo de cuenta atrás del overlay, en segundos, antes de pausar.
        /// </summary>
        public int OverlayCountdownSeconds { get; set; } = 15;

        /// <summary>
        /// Tiempo de espera, en segundos, antes de volver a mostrar el aviso
        /// después de que el usuario lo cancele.
        /// </summary>
        public int PromptCooldownSeconds { get; set; } = 300;

        /// <summary>
        /// Indica si Slumber debe iniciarse automáticamente con Windows.
        /// </summary>
        public bool StartWithWindows { get; set; } = false;
    }
}