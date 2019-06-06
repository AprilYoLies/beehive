package top.aprilyolies.curator.framework;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;

import java.util.Collection;

/**
 * @Author EvaJohnson
 * @Date 2019-06-06
 * @Email g863821569@gmail.com
 */
public class TransactionExample {
    public static void main(String[] args) throws Exception {
        CuratorFramework client = CreateClientExample.createSimple("127.0.0.1:2181");
        client.start();
        transaction(client);
    }

    public static Collection<CuratorTransactionResult> transaction(CuratorFramework client) throws Exception {
        // this example shows how to use ZooKeeper's new transactions
        Collection<CuratorTransactionResult> results = client.inTransaction().create().forPath("/path", "some data".getBytes())
                .and().setData().forPath("/path", "other data".getBytes())
                .and().delete().forPath("/path")
                .and().commit(); // IMPORTANT!
        // called
        for (CuratorTransactionResult result : results) {
            System.out.println(result.getForPath() + " - " + result.getType());
        }
        return results;
    }

    /*
     * These next four methods show how to use Curator's transaction APIs in a
     * more traditional - one-at-a-time - manner
     */
    public static CuratorTransaction startTransaction(CuratorFramework client) {
        // start the transaction builder
        return client.inTransaction();
    }

    public static CuratorTransactionFinal addCreateToTransaction(CuratorTransaction transaction) throws Exception {
        // add a create operation
        return transaction.create().forPath("/a/path", "some data".getBytes()).and();
    }

    public static CuratorTransactionFinal addDeleteToTransaction(CuratorTransaction transaction) throws Exception {
        // add a delete operation
        return transaction.delete().forPath("/another/path").and();
    }

    public static void commitTransaction(CuratorTransactionFinal transaction) throws Exception {
        // commit the transaction
        transaction.commit();
    }
}