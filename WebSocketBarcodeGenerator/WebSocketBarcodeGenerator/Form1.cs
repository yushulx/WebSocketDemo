using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

using Dynamsoft.DotNet.TWAIN;

using SuperSocket.SocketBase;
using SuperWebSocket;
namespace WebSocketBarcodeGenerator
{
    public partial class Form1 : Form
    {
        private DynamicDotNetTwain dynamicDotNetTwain;
        private WebSocketServer appServer;

        public Form1()
        {
            InitializeComponent();
            init();
        }

        private void init()
        {
            dynamicDotNetTwain = new Dynamsoft.DotNet.TWAIN.DynamicDotNetTwain(); // create Dynamic .NET TWAIN component
            appServer = new WebSocketServer();  // create WebSocket server
        }

        private void buttonStart_Click(object sender, EventArgs e)
        {
            websocket_start();
        }

        private ImageData load_image(Image img)
        {
            int width = img.Width;
            int height = img.Height;

            ImageData imageData = new ImageData();
            imageData.Width = width;
            imageData.Height = height;

            byte[] result;
            using (System.IO.MemoryStream stream = new System.IO.MemoryStream())
            {
                img.Save(stream, System.Drawing.Imaging.ImageFormat.Bmp);   // convert png to bmp
                result = stream.GetBuffer();
            }

            byte[] image = new byte[width * height * 4];

            int iIndex = 0;
            int iRowIndex = 0;
            int iPixelBytes = 4;
            if (img.PixelFormat == System.Drawing.Imaging.PixelFormat.Format24bppRgb) 
            {
                iPixelBytes = 3;
            }

            int iWidth = width * iPixelBytes;

            for (int i = height - 1; i >= 0; --i)
            {
                iRowIndex = i * iWidth;
                for (int j = 0; j < iWidth; j += iPixelBytes)
                {
                    if (iPixelBytes == 3)
                    {
                        image[iIndex++] = result[iRowIndex + j + 2 + 54]; // B
                        image[iIndex++] = result[iRowIndex + j + 1 + 54]; // G
                        image[iIndex++] = result[iRowIndex + j + 54];     // R
                        image[iIndex++] = 255;                            // A
                    }
                    else
                    {
                        image[iIndex++] = result[iRowIndex + j + 2 + 54]; // B
                        image[iIndex++] = result[iRowIndex + j + 1 + 54]; // G
                        image[iIndex++] = result[iRowIndex + j + 54];     // R
                        image[iIndex++] = result[iRowIndex + j + 3 + 54]; // A
                    }

                }
            }

            imageData.Data = image;

            return imageData;
        }

        private void websocket_start()
        {
            //Setup the appServer
            if (!appServer.Setup("192.168.8.84", 2012)) //Setup with listening port
            {
                MessageBox.Show("Failed to setup!");
                return;
            }

            //if (!appServer.Setup(2012)) //Setup with listening port
            //{
            //    MessageBox.Show("Failed to setup!");
            //    return;
            //}

            appServer.NewMessageReceived += new SessionHandler<WebSocketSession, string>(appServer_NewMessageReceived);

            //Try to start the appServer
            if (!appServer.Start())
            {
                MessageBox.Show("Failed to start!");
                return;
            }

            buttonStart.Enabled = false;
            buttonStop.Enabled = true;
        }

        private void websocket_stop()
        {
            //Stop the appServer
            appServer.Stop();

            buttonStart.Enabled = true;
            buttonStop.Enabled = false;
        }

        private void appServer_NewMessageReceived(WebSocketSession session, string message)
        {
            // generate barcode 
            float scale = 3;
            short sImageIndex = 0;
            dynamicDotNetTwain.MaxImagesInBuffer = 1;
            bool isLoaded = dynamicDotNetTwain.LoadImage("test.png");
            dynamicDotNetTwain.AddBarcode(sImageIndex, Dynamsoft.DotNet.TWAIN.Enums.Barcode.BarcodeFormat.QR_CODE, message, "", 0, 0, scale);
            Image img = dynamicDotNetTwain.GetImage(sImageIndex);

            ImageData imageData = load_image(img);
            session.Send(imageData.Width + "," + imageData.Height);
            session.Send(imageData.Data, 0, imageData.Data.Length);
            imageData = null;
        }

        private void buttonStop_Click(object sender, EventArgs e)
        {
            websocket_stop();
        }

        private class ImageData
        {
            int width, height;
            byte[] data;

            public int Width
            {
                get { return width; }
                set { width = value; }
            }

            public int Height
            {
                get { return height; }
                set { height = value; }
            }

            public byte[] Data
            {
                get { return data; }
                set { data = value; }
            }
        }
    }
}
