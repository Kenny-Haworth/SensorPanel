namespace CorsairLink.Devices.CommanderCore;

using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

public sealed class CommanderCoreTemperatureSensor
{
    public CommanderCoreTemperatureSensor(int channel, CommanderCoreTemperatureSensorStatus status, float? tempCelsius)
    {
        Channel = channel;
        Status = status;
        TempCelsius = tempCelsius;

        // Send temperature data over UDP to the SensorPanel
        sendTemperatureData();
    }

    public int Channel { get; }
    public CommanderCoreTemperatureSensorStatus Status { get; }
    public float? TempCelsius { get; }

    private void sendTemperatureData()
    {
        UdpClient udpClient = new UdpClient();
        IPEndPoint endPoint = new IPEndPoint(IPAddress.Parse("127.0.0.1"), 48620);

        byte[] bytes = Encoding.ASCII.GetBytes($"{Channel}:{TempCelsius}");
        udpClient.Send(bytes, bytes.Length, endPoint);
    }
}

public enum CommanderCoreTemperatureSensorStatus : byte
{
    Available = 0x00,
    Unavailable = 0x01,
}
