Demonstration of char encoding problems. See api_csvs/test/andrew/actonco2actions in svn.

All software involved is set to use the relevant char encoding, i.e. java code and scalc.

The following chars are replaced to be xml safe: & < > with &amp; &lt; &gt;
The following chars cause the batch to fail: £ replaced with ___GBP___ or %A3 (urlencoded)

These files work after I spent MANY hours manually deleting chars in the csv.
data_ISO-8859-1.csv works
data_UTF-8.csv works (all software set to UTF-8)

These files contain full original data, they only differ in char encoding.

data_full_ISO-8859-1.csv works
data_full_UTF-8.csv FAILS - see example_links_utf8.txt for request xml and response.

(Separate issue is that deleting and creating data categories was VERY slow via the web interface.
Had to wait minutes and in one case the category didn't disappear after it was deleted,
but couldn't view either.
This made the process of testing this and discovering problematic chars very difficult.
We should look at this problem once v2 migration is done.)
