Original description of Bug-1047736:
Link: http://sourceforge.net/tracker/index.php?func=detail&aid=1047736&group_id=44711&atid=440641
Summary: png 2c 8bit white border 1 pix saved - erode
-----------------------------------------------
png 2c 8bit white border 1 pix saved - erode
png 2c 8bit white border 1 pix saved
when process binary erode was carried out
on left upper en lower side of the image, 4pix from the
right border

border 3 pix when 3 x erosion
same distance from right border

input image
PNG 300x300+0+0 PseudoClass 2c 1-bit

png saved as
PNG 300x300+0+0 PseudoClass 2c 8-bi

this doesn't happen with eg tiff format
saved as
TIFF 300x300+0+0 PseudoClass 256c 8-bit
-----------------------------------------------