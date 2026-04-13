using NAudio.CoreAudioApi;
using NAudio.CoreAudioApi.Interfaces;
using System;
using System.Data;

namespace Slumber.Agent.Windows.Services
{
    public class MediaSessionService
    {
        public bool IsAudioPlaying()
        {
            using var deviceEnumerator = new MMDeviceEnumerator();
            var device = deviceEnumerator.GetDefaultAudioEndpoint(DataFlow.Render, Role.Multimedia);

            var sessions = device.AudioSessionManager.Sessions;

            for (int i = 0; i < sessions.Count; i++)
            {
                var session = sessions[i];

                // Más fiable: mirar volumen + estado activo
                if (session.State == AudioSessionState.AudioSessionStateActive &&
                    session.AudioMeterInformation.MasterPeakValue > 0.01)
                {
                    return true;
                }
            }

            return false;
        }
    }
}