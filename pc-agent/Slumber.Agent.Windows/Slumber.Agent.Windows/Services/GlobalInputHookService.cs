using System;
using System.Diagnostics;
using System.Runtime.InteropServices;

namespace Slumber.Agent.Windows.Services
{
    public class GlobalInputHookService : IDisposable
    {
        private const int WH_KEYBOARD_LL = 13;
        private const int WH_MOUSE_LL = 14;

        private IntPtr _keyboardHookId = IntPtr.Zero;
        private IntPtr _mouseHookId = IntPtr.Zero;

        private LowLevelKeyboardProc? _keyboardProc;
        private LowLevelMouseProc? _mouseProc;

        public event EventHandler? UserActivityDetected;

        public void Start()
        {
            _keyboardProc = KeyboardHookCallback;
            _mouseProc = MouseHookCallback;

            _keyboardHookId = SetKeyboardHook(_keyboardProc);
            _mouseHookId = SetMouseHook(_mouseProc);
        }

        public void Stop()
        {
            if (_keyboardHookId != IntPtr.Zero)
            {
                UnhookWindowsHookEx(_keyboardHookId);
                _keyboardHookId = IntPtr.Zero;
            }

            if (_mouseHookId != IntPtr.Zero)
            {
                UnhookWindowsHookEx(_mouseHookId);
                _mouseHookId = IntPtr.Zero;
            }
        }

        public void Dispose()
        {
            Stop();
            GC.SuppressFinalize(this);
        }

        private IntPtr SetKeyboardHook(LowLevelKeyboardProc proc)
        {
            using Process curProcess = Process.GetCurrentProcess();
            using ProcessModule? curModule = curProcess.MainModule;

            return SetWindowsHookEx(
                WH_KEYBOARD_LL,
                proc,
                GetModuleHandle(curModule?.ModuleName),
                0);
        }

        private IntPtr SetMouseHook(LowLevelMouseProc proc)
        {
            using Process curProcess = Process.GetCurrentProcess();
            using ProcessModule? curModule = curProcess.MainModule;

            return SetWindowsHookEx(
                WH_MOUSE_LL,
                proc,
                GetModuleHandle(curModule?.ModuleName),
                0);
        }

        private IntPtr KeyboardHookCallback(int nCode, IntPtr wParam, IntPtr lParam)
        {
            if (nCode >= 0)
            {
                UserActivityDetected?.Invoke(this, EventArgs.Empty);
            }

            return CallNextHookEx(_keyboardHookId, nCode, wParam, lParam);
        }

        private IntPtr MouseHookCallback(int nCode, IntPtr wParam, IntPtr lParam)
        {
            if (nCode >= 0)
            {
                UserActivityDetected?.Invoke(this, EventArgs.Empty);
            }

            return CallNextHookEx(_mouseHookId, nCode, wParam, lParam);
        }

        private delegate IntPtr LowLevelKeyboardProc(int nCode, IntPtr wParam, IntPtr lParam);
        private delegate IntPtr LowLevelMouseProc(int nCode, IntPtr wParam, IntPtr lParam);

        [DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        private static extern IntPtr SetWindowsHookEx(
            int idHook,
            Delegate lpfn,
            IntPtr hMod,
            uint dwThreadId);

        [DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        [return: MarshalAs(UnmanagedType.Bool)]
        private static extern bool UnhookWindowsHookEx(IntPtr hhk);

        [DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        private static extern IntPtr CallNextHookEx(
            IntPtr hhk,
            int nCode,
            IntPtr wParam,
            IntPtr lParam);

        [DllImport("kernel32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        private static extern IntPtr GetModuleHandle(string? lpModuleName);
    }
}