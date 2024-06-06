package simpledb;

import java.io.*;

import java.util.concurrent.ConcurrentHashMap;

import java.util.*;

/**
 * BufferPool manages the reading and writing of pages into memory from
 * disk. Access methods call into it to retrieve pages, and it fetches
 * pages from the appropriate location.
 * <p>
 * The BufferPool is also responsible for locking;  when a transaction fetches
 * a page, BufferPool checks that the transaction has the appropriate
 * locks to read/write the page.
 * 
 * @Threadsafe, all fields are final
 */
public class BufferPool {
    /** Bytes per page, including header. */
    private static final int PAGE_SIZE = 4096;

    private static int pageSize = PAGE_SIZE;
    
    /** Default number of pages passed to the constructor. This is used by
    other classes. BufferPool should use the numPages argument to the
    constructor instead. */
    public static final int DEFAULT_PAGES = 50;
    
    private final int maxNumPages;
    private ConcurrentHashMap<PageId, LinkNode> pages;
    /**
     * Creates a BufferPool that caches up to numPages pages.
     *
     * @param numPages maximum number of pages in this buffer pool.
     */
     
    // class LinkNode
     private class LinkNode{
        PageId pageId;
        Page page;
        LinkNode prev;
        LinkNode next;
        public LinkNode(PageId pageId, Page page){
            this.pageId = pageId;
            this.page = page;
        }
     }
     
     LinkNode head;
     LinkNode tail;
     
    private void addToHead(LinkNode n){
        n.prev = head;
        n.next = head.next;
        head.next.prev = n;
        head.next = n;
    }
    
    private void remove(LinkNode node){
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
    
    private void moveToHead(LinkNode node){
        remove(node);
        addToHead(node);
    }
    
    private LinkNode removeLast(){
        LinkNode node = tail.prev;
        remove(node);
        return node;
    }
     
    class PageLock{
        private static final int SHARE = 0;
        private static final int EXCLUSIVE = 1;
        private TransactionId tid;
        private int type;
        public PageLock(TransactionId tid, int type){
            this.tid = tid;
            this.type = type;
        }
        public TransactionId getTid(){
            return tid;
        }
        public int getType(){
            return type;
        }
        public void setType(int type){
            this.type = type;
        }
    }

    class LockManager {
        ConcurrentHashMap<PageId, ConcurrentHashMap<TransactionId, PageLock>> lockMap = new ConcurrentHashMap<>();
        
        // get lock
        public synchronized boolean acquiredLock(PageId pageId, TransactionId tid, int requiredType) {
            // judge whther the current page is locked
            if (lockMap.get(pageId) == null) {
                // create lock
                PageLock pageLock = new PageLock(tid, requiredType);
                ConcurrentHashMap<TransactionId, PageLock> pageLocks = new ConcurrentHashMap<>();
                pageLocks.put(tid, pageLock);
                lockMap.put(pageId, pageLocks);
                return true;
            }
            
            // get waiting queue
            ConcurrentHashMap<TransactionId, PageLock> pageLocks = lockMap.get(pageId);

            if (pageLocks.get(tid) == null) {
                if (pageLocks.size() > 1) {
                    if (requiredType == PageLock.SHARE) {
                        PageLock pageLock = new PageLock(tid, PageLock.SHARE);
                        pageLocks.put(tid, pageLock);
                        lockMap.put(pageId, pageLocks);
                        return true;
                    }
                    // case exlucsive lock
                    else if (requiredType == PageLock.EXCLUSIVE) {
                        return false;
                    }
                }
                if (pageLocks.size() == 1) {
                    // there is another transaction in the page
                    PageLock curLock = null;
                    for (PageLock lock : pageLocks.values()) {
                        curLock = lock;
                    }
                    if (curLock.getType() == PageLock.SHARE) {
                        // if the request is share lock
                        if (requiredType == PageLock.SHARE) {
                            PageLock pageLock = new PageLock(tid, PageLock.SHARE);
                            pageLocks.put(tid, pageLock);
                            lockMap.put(pageId, pageLocks);
                            return true;
                        }
                        // if the lock is occupied type
                        else if (requiredType == PageLock.EXCLUSIVE) {
                            return false;
                        }
                    }
                    else if (curLock.getType() == PageLock.EXCLUSIVE) {
                        return false;
                    }
                }
            }

            else if (pageLocks.get(tid) != null) {
                PageLock pageLock = pageLocks.get(tid);
                if (pageLock.getType() == PageLock.SHARE) {
                    if (requiredType == PageLock.SHARE) {
                        return true;
                    }
                    else if (requiredType == PageLock.EXCLUSIVE) {
                        if (pageLocks.size() == 1) {
                            pageLock.setType(PageLock.EXCLUSIVE);
                            pageLocks.put(tid, pageLock);
                            return true;
                        }
                        else if (pageLocks.size() > 1) {
                            return false;
                        }
                    }
                }
                return pageLock.getType() == PageLock.EXCLUSIVE;
            }
            return false;
        }
        
        
    public synchronized boolean releaseLock(TransactionId tid, PageId pageId) {
            // 判断是否持有锁
            if (isHoldLock(tid, pageId)) {
                ConcurrentHashMap<TransactionId, PageLock> locks = lockMap.get(pageId);
                locks.remove(tid);
                if (locks.size() == 0) {
                    lockMap.remove(pageId);
                }
                return true;
            }
            return false;
        }
        
    public synchronized boolean isHoldLock(TransactionId tid, PageId pageId) {
            ConcurrentHashMap<TransactionId, PageLock> locks = lockMap.get(pageId);
            if (locks == null) {
                return false;
            }
            PageLock pageLock = locks.get(tid);
            if (pageLock == null) {
                return false;
            }
            return true;
        }

    public synchronized void completeTranslation(TransactionId tid) {
            for (PageId pageId : lockMap.keySet()) {
                releaseLock(tid, pageId);
            }
        }
       }
       
       
    private LockManager lockManager;



    public BufferPool(int numPages) {
        // some code goes here     
        this.maxNumPages = numPages;
        pages = new ConcurrentHashMap<>();
        head = new LinkNode(new HeapPageId(-1, -1), null);
        tail = new LinkNode(new HeapPageId(-1, -1), null);
        head.next = tail;
        tail.prev = head;
        lockManager = new LockManager();

    }
    
    public static int getPageSize() {
      return pageSize;
    }
    
    // THIS FUNCTION SHOULD ONLY BE USED FOR TESTING!!
    public static void setPageSize(int pageSize) {
    	BufferPool.pageSize = pageSize;
    }
    
    // THIS FUNCTION SHOULD ONLY BE USED FOR TESTING!!
    public static void resetPageSize() {
    	BufferPool.pageSize = PAGE_SIZE;
    }

    /**
     * Retrieve the specified page with the associated permissions.
     * Will acquire a lock and may block if that lock is held by another
     * transaction.
     * <p>
     * The retrieved page should be looked up in the buffer pool.  If it
     * is present, it should be returned.  If it is not present, it should
     * be added to the buffer pool and returned.  If there is insufficient
     * space in the buffer pool, an page should be evicted and the new page
     * should be added in its place.
     *
     * @param tid the ID of the transaction requesting the page
     * @param pid the ID of the requested page
     * @param perm the requested permissions on the page
     */
    public  Page getPage(TransactionId tid, PageId pid, Permissions perm)
        throws TransactionAbortedException, DbException {
        // some code goes here     
        int lockType = perm == Permissions.READ_ONLY ? PageLock.SHARE : PageLock.EXCLUSIVE;
        // calculate excess time
        long startTime = System.currentTimeMillis();
        boolean isAcquired = false;

        while(!isAcquired){
            isAcquired = lockManager.acquiredLock(pid, tid, lockType);
            long now = System.currentTimeMillis();
            if(now - startTime > 500){
                throw new TransactionAbortedException();
            }
        }


        if(!pages.containsKey(pid)){
            DbFile dbFile = Database.getCatalog().getDatabaseFile(pid.getTableId());
            Page page = dbFile.readPage(pid);
            if(pages.size() >= maxNumPages){
                evictPage();
            }
            LinkNode node = new LinkNode(pid, page);
            pages.put(pid, node);
            addToHead(node);
        }

        moveToHead(pages.get(pid));
        return pages.get(pid).page;

    }

    /**
     * Releases the lock on a page.
     * Calling this is very risky, and may result in wrong behavior. Think hard
     * about who needs to call this and why, and why they can run the risk of
     * calling it.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param pid the ID of the page to unlock
     */
    public  void releasePage(TransactionId tid, PageId pid) {
        // some code goes here      
        // not necessary for lab1|lab2
        lockManager.releaseLock(tid, pid);
    }
    
    public  void unsafeReleasePage(TransactionId tid, PageId pid) {
        // some code goes here
        // not necessary fosr lab1|lab2
        lockManager.releaseLock(tid, pid);
    }


    /**
     * Release all locks associated with a given transaction.
     *
     * @param tid the ID of the transaction requesting the unlock
     */
    public void transactionComplete(TransactionId tid) throws IOException {
        // some code goes here     
        // not necessary for lab1|lab2
        transactionComplete(tid,true);
    }

    /** Return true if the specified transaction has a lock on the specified page */
    public boolean holdsLock(TransactionId tid, PageId p) {
        // some code goes here      
        // not necessary for lab1|lab2
        return false;
    }
    
    
    public synchronized void restorePages(TransactionId tid){
        for(LinkNode node : pages.values()){
            PageId pageId = node.pageId;
            Page page = node.page;
            if(tid.equals(page.isDirty())){
                int tableId = pageId.getTableId();
                DbFile table = Database.getCatalog().getDatabaseFile(tableId);
                Page pageFromDisk = table.readPage(pageId);
                node.page = pageFromDisk;
                pages.put(pageId, node);
                moveToHead(node);
            }
        }
    }

    /**
     * Commit or abort a given transaction; release all locks associated to
     * the transaction.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param commit a flag indicating whether we should commit or abort
     */
    public void transactionComplete(TransactionId tid, boolean commit)
        throws IOException {
        // some code goes here
        // not necessary for lab1|lab2
        if(commit){
        // update pages
        try{
            flushPages(tid);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    // roll back when fail to submit
    else{
        restorePages(tid);
    }
    lockManager.completeTranslation(tid);

    }
    
    
    private void updateBufferPool(List<Page> pageList, TransactionId tid) throws DbException{
        for(Page page: pageList){
            page.markDirty(true, tid);
            //evict page when cache is full
            if(pages.size() > maxNumPages){
                evictPage();
            }
            LinkNode node;
            
            if(pages.containsKey(page.getId())){
               node = pages.get(page.getId());
               node.page = page;
            }
            
            else{
                if(pages.size()>=maxNumPages) evictPage();
                node = new LinkNode(page.getId(),page);
                addToHead(node);
            }
            pages.put(page.getId(),node);
            
            
        }   
    
    
    }
    
    
    

    /**
     * Add a tuple to the specified table on behalf of transaction tid.  Will
     * acquire a write lock on the page the tuple is added to and any other 
     * pages that are updated (Lock acquisition is not needed for lab2). 
     * May block if the lock(s) cannot be acquired.
     * 
     * Marks any pages that were dirtied by the operation as dirty by calling
     * their markDirty bit, and adds versions of any pages that have 
     * been dirtied to the cache (replacing any existing versions of those pages) so 
     * that future requests see up-to-date pages. 
     *
     * @param tid the transaction adding the tuple
     * @param tableId the table to add the tuple to
     * @param t the tuple to add
     */
    public void insertTuple(TransactionId tid, int tableId, Tuple t)
        throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        // not necessary for lab1
        DbFile dbFile = Database.getCatalog().getDatabaseFile(tableId);
        updateBufferPool(dbFile.insertTuple(tid,t),tid);
        
     
    }

    /**
     * 
     
      the specified tuple from the buffer pool.
     * Will acquire a write lock on the page the tuple is removed from and any
     * other pages that are updated. May block if the lock(s) cannot be acquired.
     *
     * Marks any pages that were dirtied by the operation as dirty by calling
     * their markDirty bit, and adds versions of any pages that have 
     * been dirtied to the cache (replacing any existing versions of those pages) so 
     * that future requests see up-to-date pages. 
     *
     * @param tid the transaction deleting the tuple.
     * @param t the tuple to delete
     */
    public  void deleteTuple(TransactionId tid, Tuple t)
        throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        // not necessary for lab1
        DbFile dbFile= Database.getCatalog().getDatabaseFile(t.getRecordId().getPageId().getTableId());
        updateBufferPool(dbFile.deleteTuple(tid,t), tid);
    }
    
    
    

    /**
     * Flush all dirty pages to disk.
     * NB: Be careful using this routine -- it writes dirty data to disk so will
     *     break simpledb if running in NO STEAL mode.
     */
    public synchronized void flushAllPages() throws IOException {
        // some code goes here
        // not necessary for lab1
        for(PageId pageId: pages.keySet()){
            flushPage(pageId);
        }

    }

    /** Remove the specific page id from the buffer pool.
        Needed by the recovery manager to ensure that the
        buffer pool doesn't keep a rolled back page in its
        cache.
        
        Also used by B+ tree files to ensure that deleted pages
        are removed from the cache so they can be reused safely
    */
    public synchronized void discardPage(PageId pid) {
        // some code goes here
        // not necessary for lab1
        if(pages.containsKey(pid)){
            remove(pages.get(pid));
            pages.remove(pid);
        }
        
    }

    /**
     * Flushes a certain page to disk
     * @param pid an ID indicating the page to flush
     */
    private synchronized  void flushPage(PageId pid) throws IOException {
        // some code goes here
        // not necessary for lab1
        Page flush_page = pages.get(pid).page;
        if(flush_page.isDirty() != null){
            Database.getCatalog().getDatabaseFile(pid.getTableId()).writePage(flush_page);
            flush_page.markDirty(false, null);
        }
    }

    /** Write all pages of the specified transaction to disk.
     */
    public synchronized  void flushPages(TransactionId tid) throws IOException {
        // some code goes here
        // not necessary for lab1|lab2
        for(LinkNode node : pages.values()){
            PageId pageId = node.pageId;
            Page page = node.page;
            if(tid.equals(page.isDirty())){
                flushPage(pageId);
            }
        }
    }

    /**
     * Discards a page from the buffer pool.
     * Flushes the page to disk to ensure dirty pages are updated on disk.
     */
    private synchronized  void evictPage() throws DbException {
        // some code goes here
        // not necessary for lab1
        for (int i = 0; i < maxNumPages; i++) {
        // evict the last node
        LinkNode node = removeLast();
        Page evictPage = node.page;
        if(evictPage.isDirty() != null){
            addToHead(node);
        }
        // flush all pages
        else{
            // update pages
            try{
                flushPage(node.pageId);
            }catch (IOException e){
                e.printStackTrace();
            }
            pages.remove(node.pageId);
            return ;
        }
    }

    throw new DbException("All Page Are Dirty Page");

    }

}
