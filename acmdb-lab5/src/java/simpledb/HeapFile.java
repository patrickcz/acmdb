package simpledb;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    private final File file;
    private final TupleDesc tupleDesc;
    
     
    public HeapFile(File f, TupleDesc td) {
        // some code goes here    
        this.file = f;
        this.tupleDesc = td;

    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here    
        return this.file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here  
        return this.file.getAbsoluteFile().hashCode();
        //throw new UnsupportedOperationException("implement this");
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here     
        return this.tupleDesc;
        //throw new UnsupportedOperationException("implement this");
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here    
        int tableId = pid.getTableId();
        int pageNum = pid.pageNumber();
        
        int pageSize = Database.getBufferPool().getPageSize();
        long offset = pageNum * pageSize;
        
        HeapPage page = null;
        RandomAccessFile rfile = null;
        
        try{
            rfile = new RandomAccessFile(file, "r");
            byte[] data = new byte[pageSize];
            rfile.seek(offset);
            rfile.read(data);
            HeapPageId hpid = new HeapPageId(tableId, pageNum);
            page = new HeapPage(hpid, data);

        } 
        catch (IOException e){
            e.printStackTrace();
        }finally{
            try{
                rfile.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        
        }
        return page;
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1   
        int pageSize = BufferPool.getPageSize();
        int pageNumber = page.getId().pageNumber();
        
        if(pageNumber > numPages()) throw new IllegalArgumentException();
        
        RandomAccessFile f = new RandomAccessFile(file, "rw");
        f.seek(pageNumber*pageSize);
        f.write(page.getPageData());
        f.close();

    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here     
        return (int)(file.length()/Database.getBufferPool().getPageSize());
    }


    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here    
        // not necessary for lab1

    ArrayList<Page> list = new ArrayList<>();

    for (int pageNo = 0; pageNo < numPages(); pageNo++) {
        HeapPageId pageId = new HeapPageId(getId(), pageNo);
        HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid, pageId, Permissions.READ_WRITE);
        if(page.getNumEmptySlots() != 0){
            page.insertTuple(t);
            list.add(page);
            return list;
        }
        else{
            Database.getBufferPool().unsafeReleasePage(tid, pageId);
        }
    }

    BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file, true));
    // create a new empty page
    byte[] emptyPage = HeapPage.createEmptyPageData();
    output.write(emptyPage);
    output.close();

    // create new page
    HeapPageId pageId = new HeapPageId(getId(), numPages() - 1);
    HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid, pageId, Permissions.READ_WRITE);
    page.insertTuple(t);
    list.add(page);
    return list;

    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here  
        HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid, t.getRecordId().getPageId(), Permissions.READ_WRITE);
        page.deleteTuple(t);
        ArrayList<Page> modified = new ArrayList<>();
        modified.add(page);
        return modified;

        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here     
        return new HeapFileIterator(this, tid);
    }
    
    private class HeapFileIterator implements DbFileIterator{
         private final TransactionId transactionId;
         private Iterator<Tuple> tupleIter;
         private int curPageNo;
         private final HeapFile heapFile;
         private int numPages;
         
         // constructor
         public HeapFileIterator(HeapFile file, TransactionId tid){
             this.transactionId = tid;
             this.heapFile = file;
             this.numPages = this.heapFile.numPages();
             this.curPageNo = 0;
         }
         

         public Iterator<Tuple> getPageTuples(HeapPageId hpid) throws DbException, TransactionAbortedException{
             BufferPool pool = Database.getBufferPool();
             HeapPage page = (HeapPage) pool.getPage(transactionId, hpid, Permissions.READ_ONLY);
             return page.iterator();
         
         }
         
         
         @Override
         public void open() throws DbException, TransactionAbortedException{
             this.curPageNo = 0;
             HeapPageId pid = new HeapPageId(this.heapFile.getId(),0); 
             this.tupleIter = getPageTuples(pid);
         }
         
         @Override
         public boolean hasNext() throws DbException, TransactionAbortedException{
             if(this.tupleIter == null) return false;
             if(this.tupleIter.hasNext()) return true;
             else{
                 this.curPageNo++;
                 if(curPageNo >= numPages) return false;
                 this.tupleIter = this.getPageTuples(new HeapPageId(this.heapFile.getId(), curPageNo));
                 if(this.tupleIter == null) return false;
                 return this.tupleIter.hasNext();
             }
             
         }
          // pay attetion to the order of two judge conditions in line 189
         @Override
         public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException{
             if(tupleIter == null || !tupleIter.hasNext()) throw new NoSuchElementException();
             return tupleIter.next();
         }
         
         @Override
         public void close(){
             curPageNo = 0;
             tupleIter = null;
         }
         
         @Override
         // set state as initial state and traverse from the first page of heapFile
         public void rewind() throws DbException, TransactionAbortedException{
             curPageNo = 0;
             close();
             open();
         }
        
    
    
    
    
    }

}

