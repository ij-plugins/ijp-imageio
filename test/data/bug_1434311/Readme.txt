Original description of Bug-1434311:
Link: http://sourceforge.net/tracker/index.php?func=detail&aid=1434311&group_id=44711&atid=440641
Summary: ij-imageio: Problem saving 1 bit TIFF images
-----------------------------------------------
This problem was originally reported by Herman on
ij-plugins mailing list:
http://sourceforge.net/mailarchive/forum.php?thread_id=9714604&forum_id=40439

On 2006-02-13 Herman wrote:

I'm having some problems with imagej when I try save an
image in tiff format with G4 compression. I can save
an image with these properties, but when i try open it
with an image viewer (I'm using Irfan), it shows a
white image, and the size isn't correct. An image that
weights 44.2 KB , after g4 compression is saved at 4.9
KB, but there isn't information about compression in
the property page.

I attached the images.
Note> testG4.tif is an original image and testG4out.tif
is an saved image with IJImageIO plugin.
-----------------------------------------------
Attached images: testG4.tif, testG4out.tif
-----------------------------------------------