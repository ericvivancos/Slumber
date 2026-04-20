using Slumber.Agent.Windows.Models;
using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;

namespace Slumber.Agent.Windows.Services
{
    public class SlumberLanService : IDisposable
    {
        public const int DefaultPort = 34821;

        private readonly Func<SlumberServiceIdentity> _snapshotFactory;
        private readonly JsonSerializerOptions _jsonOptions = new(JsonSerializerDefaults.Web);
        private readonly CancellationTokenSource _cts = new();
        private TcpListener? _listener;
        private Task? _serverTask;

        public SlumberLanService(Func<SlumberServiceIdentity> snapshotFactory)
        {
            _snapshotFactory = snapshotFactory;
        }

        public void Start()
        {
            if (_listener != null)
            {
                return;
            }

            _listener = new TcpListener(IPAddress.Any, DefaultPort);
            _listener.Start();
            _serverTask = Task.Run(() => AcceptLoopAsync(_cts.Token));
        }

        public void Stop()
        {
            _cts.Cancel();
            _listener?.Stop();
            _listener = null;
        }

        private async Task AcceptLoopAsync(CancellationToken cancellationToken)
        {
            if (_listener == null)
            {
                return;
            }

            while (!cancellationToken.IsCancellationRequested)
            {
                TcpClient? client = null;

                try
                {
                    client = await _listener.AcceptTcpClientAsync(cancellationToken);
                    _ = Task.Run(() => HandleClientAsync(client, cancellationToken), cancellationToken);
                }
                catch (OperationCanceledException)
                {
                    break;
                }
                catch
                {
                    client?.Dispose();
                }
            }
        }

        private async Task HandleClientAsync(TcpClient client, CancellationToken cancellationToken)
        {
            using (client)
            {
                using var stream = client.GetStream();
                using var reader = new StreamReader(stream, Encoding.ASCII, false, 1024, true);
                using var writer = new StreamWriter(stream, new UTF8Encoding(false), 1024, true)
                {
                    NewLine = "\r\n",
                    AutoFlush = true
                };

                try
                {
                    var requestLine = await reader.ReadLineAsync(cancellationToken);
                    if (string.IsNullOrWhiteSpace(requestLine))
                    {
                        return;
                    }

                    string? line;
                    do
                    {
                        line = await reader.ReadLineAsync(cancellationToken);
                    }
                    while (!string.IsNullOrEmpty(line));

                    var path = ParsePath(requestLine);

                    if (path == "/identity")
                    {
                        var snapshot = _snapshotFactory();
                        await WriteJsonResponseAsync(writer, HttpStatusCode.OK, snapshot);
                        return;
                    }

                    if (path == "/health")
                    {
                        await WriteJsonResponseAsync(writer, HttpStatusCode.OK, new { status = "ok" });
                        return;
                    }

                    await WriteJsonResponseAsync(writer, HttpStatusCode.NotFound, new { error = "not-found" });
                }
                catch
                {
                    // Ignore transient socket errors so the desktop app keeps running.
                }
            }
        }

        private async Task WriteJsonResponseAsync(
            StreamWriter writer,
            HttpStatusCode statusCode,
            object payload)
        {
            string body = JsonSerializer.Serialize(payload, _jsonOptions);
            byte[] bodyBytes = Encoding.UTF8.GetBytes(body);

            await writer.WriteLineAsync($"HTTP/1.1 {(int)statusCode} {statusCode}");
            await writer.WriteLineAsync("Content-Type: application/json; charset=utf-8");
            await writer.WriteLineAsync($"Content-Length: {bodyBytes.Length}");
            await writer.WriteLineAsync("Connection: close");
            await writer.WriteLineAsync();
            await writer.WriteAsync(body);
            await writer.FlushAsync();
        }

        private static string ParsePath(string requestLine)
        {
            var parts = requestLine.Split(' ', StringSplitOptions.RemoveEmptyEntries);
            if (parts.Length < 2)
            {
                return "/";
            }

            return parts[1];
        }

        public void Dispose()
        {
            Stop();
            _cts.Dispose();
        }
    }
}
