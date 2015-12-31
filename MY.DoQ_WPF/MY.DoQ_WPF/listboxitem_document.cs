using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MY.DoQ_WPF
{
    public class listboxitem_document
    {
        public listboxitem_document(string name, string file, Format format, favorite favorite)
        {
            this.name = name;
            this.file = file;
            this.format = format;
            this.favorite = favorite;
            switch (format)
            {
                case Format.hwp:
                    this.format_image = "C:\\Users\\user\\Documents\\Visual Studio 2012\\Projects\\MY.DoQ_WPF\\MY.DoQ_WPF\\file_hwp_small.png";
                    break;
                case Format.pdf:
                    this.format_image = "C:\\Users\\user\\Documents\\Visual Studio 2012\\Projects\\MY.DoQ_WPF\\MY.DoQ_WPF\\file_pdf_small.png";
                    break;
                case Format.ppt:
                    this.format_image = "C:\\Users\\user\\Documents\\Visual Studio 2012\\Projects\\MY.DoQ_WPF\\MY.DoQ_WPF\\file_ppt_small.PNG";
                    break;
                case Format.word:
                    this.format_image = "C:\\Users\\user\\Documents\\Visual Studio 2012\\Projects\\MY.DoQ_WPF\\MY.DoQ_WPF\\file_word_small.png";
                    break;
            }
            switch (favorite)
            {
                case favorite.YES:
                    this.fav_image = "C:\\Users\\user\\Documents\\Visual Studio 2012\\Projects\\MY.DoQ_WPF\\MY.DoQ_WPF\\favorite_on_small.png";
                    break;
                case favorite.NO:
                    this.fav_image = "C:\\Users\\user\\Documents\\Visual Studio 2012\\Projects\\MY.DoQ_WPF\\MY.DoQ_WPF\\favorite_off_small.png";
                    break;
            }
        }

        public string name { get; set; }
        public string file { get; set; }
        public string fav_image { get; set; }
        public string format_image { get; set; }


        //public string image_source { get; set; }
        public favorite favorite { get; set; }
        public Format format { get; set; }
        /*
        public override string ToString()
        {
            return String.Format("{0} {1} {2}", name, file, image_source);
        }
         */
    }
    public enum Format
    {
        hwp,
        pdf,
        ppt,
        word
    }
    public enum favorite
    {
        YES,
        NO
    }
}
