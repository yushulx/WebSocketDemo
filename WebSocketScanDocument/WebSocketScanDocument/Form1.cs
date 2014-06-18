using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.IO;

using Dynamsoft.DotNet.TWAIN;

using SuperSocket.SocketBase;
using SuperWebSocket;
using Newtonsoft.Json;

namespace WebSocketScanDocument
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
            dynamicDotNetTwain.OnPostAllTransfers += new Dynamsoft.DotNet.TWAIN.Delegate.OnPostAllTransfersHandler(this.dynamicDotNetTwain_OnPostAllTransfers);
            dynamicDotNetTwain.MaxImagesInBuffer = 64;
            dynamicDotNetTwain.IfAppendImage = true;
            dynamicDotNetTwain.IfThrowException = true;
            dynamicDotNetTwain.IfShowUI = false;

            appServer = new WebSocketServer();  // create WebSocket server
        }

        private void buttonStart_Click(object sender, EventArgs e)
        {
            websocket_start();
        }

        private static Image resizeImage(Image img, Size size)
        {
           return (Image)(new Bitmap(img, size));
        }

        private ImageData load_image(Image img)
        {
            int width = img.Width;
            int height = img.Height;

            int targetWidth = 480, targetHeight = 640;
            int wRatio = 1, hRatio = 1;

            if (width > targetWidth)
            {
                wRatio = width / targetWidth;
            }

            if (height > targetHeight)
            {
                hRatio = height / targetHeight;
            }

            if (wRatio > 1 || hRatio > 1)
            {
                if (wRatio > hRatio)
                {
                    width = targetWidth;
                    height = ((height / wRatio) >> 2) << 2;
                    img = resizeImage(img, new Size(width, height));
                }
                else
                {
                    width = ((width / hRatio) >> 2) << 2;
                    height = targetHeight;
                    img = resizeImage(img, new Size(width, height));
                }

            }

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

            int offset = 54; 
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
                        image[iIndex++] = result[iRowIndex + j + 2 + offset]; // B
                        image[iIndex++] = result[iRowIndex + j + 1 + offset]; // G
                        image[iIndex++] = result[iRowIndex + j + offset];     // R
                        image[iIndex++] = 255;                            // A
                    }
                    else
                    {
                        image[iIndex++] = result[iRowIndex + j + 2 + offset]; // B
                        image[iIndex++] = result[iRowIndex + j + 1 + offset]; // G
                        image[iIndex++] = result[iRowIndex + j + offset];     // R
                        image[iIndex++] = result[iRowIndex + j + 3 + offset]; // A
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
            appServer.NewSessionConnected += new SessionHandler<WebSocketSession>(appServer_NewSessionConnected);

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
            int iIndex = Int32.Parse(message);

            try
            {
                dynamicDotNetTwain.CloseSource();
                bool success = dynamicDotNetTwain.SelectSourceByIndex(Convert.ToInt16(iIndex));
                dynamicDotNetTwain.OpenSource();
                dynamicDotNetTwain.AcquireImage();
            }
            catch (Dynamsoft.DotNet.TWAIN.TwainException exp)
            {
                String errorstr = "";
                errorstr += "Error " + exp.Code + "\r\n" + "Description: " + exp.Message + "\r\nPosition: " + exp.TargetSite + "\r\nHelp: " + exp.HelpLink + "\r\n";
                MessageBox.Show(errorstr);
            }
            catch (Exception exp)
            {
                String errorstr = "";
                errorstr += "ErrorMessage: " + exp.Message + "\r\n";
                MessageBox.Show(errorstr);
            }
        }

        private void dynamicDotNetTwain_OnPostAllTransfers()
        {
            if (dynamicDotNetTwain.MaxImagesInBuffer < 1)
            {
                MessageBox.Show("no image");
                return;
            }

            Image img = dynamicDotNetTwain.GetImage(0);

            ImageData imageData = load_image(img);

            /* send width & height in JSON */
            StringBuilder sb = new StringBuilder();
            StringWriter sw = new StringWriter(sb);
            using (JsonWriter writer = new JsonTextWriter(sw))
            {
                writer.Formatting = Formatting.Indented;

                writer.WriteStartObject();
                writer.WritePropertyName("Draw");
                writer.WriteStartArray();
                writer.WriteValue(imageData.Width);
                writer.WriteValue(imageData.Height);
                writer.WriteEnd();
                writer.WriteEndObject();
            }
            String msg = sw.ToString();

            IEnumerable<WebSocketSession> sessions = appServer.GetAllSessions();
            foreach (WebSocketSession session in sessions) 
            {
                session.Send(msg);
                session.Send(imageData.Data, 0, imageData.Data.Length);
            }
            
            imageData = null;
        }

        private void appServer_NewSessionConnected(WebSocketSession session)
        {
            int iIndex;
            dynamicDotNetTwain.OpenSourceManager();

            StringBuilder sb = new StringBuilder();
            StringWriter sw = new StringWriter(sb);
            using (JsonWriter writer = new JsonTextWriter(sw))
            {
                writer.Formatting = Formatting.Indented;

                writer.WriteStartObject();
                writer.WritePropertyName("Sources");
                writer.WriteStartArray();
                for (iIndex = 0; iIndex < dynamicDotNetTwain.SourceCount; iIndex++)
                {
                    writer.WriteValue(dynamicDotNetTwain.SourceNameItems(Convert.ToInt16(iIndex)));
                }
                writer.WriteEnd();
                writer.WriteEndObject();
            }

            String msg = sw.ToString();
            session.Send(msg);
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
