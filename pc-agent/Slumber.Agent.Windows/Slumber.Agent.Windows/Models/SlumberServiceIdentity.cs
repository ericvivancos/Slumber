using System.Collections.Generic;

namespace Slumber.Agent.Windows.Models
{
    public class SlumberServiceIdentity
    {
        public string DeviceId { get; set; } = string.Empty;
        public string DeviceName { get; set; } = string.Empty;
        public string Host { get; set; } = string.Empty;
        public int Port { get; set; }
        public string ServiceVersion { get; set; } = string.Empty;
        public List<string> Capabilities { get; set; } = new();
        public string Availability { get; set; } = "available";
        public bool AudioPlaying { get; set; }
        public bool IsIdle { get; set; }
        public int IdleSeconds { get; set; }
    }
}
