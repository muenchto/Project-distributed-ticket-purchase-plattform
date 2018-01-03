package auxiliary;

import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZKUtils {

	public static List<String> getAllNodes(ZooKeeper zk, String path){
		Stat node;
		try {
			node = zk.exists(path, false);
			List<String> children = null;
			if (node != null) {
				children = zk.getChildren(path, false);
				java.util.Collections.sort(children);
			}
			return children;
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
		}		
		return null;
	}	
}
