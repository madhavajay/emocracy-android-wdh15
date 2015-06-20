package wdh15.emocracy;

/**
 * Created by madhavajay on 20/06/15.
 */
public class ChannelModel {
    public int id;
    public String name;
    public int yes;
    public int no;
    public int alive;
    public Integer democracy;
    public long timestamp;

    public String toString() {
        return "id " + id + " name " + name + " yes " + yes + " no " + no + " alive " + alive + " democracy " + democracy + " timestamp " + timestamp;
    }

    /*
{
  "channels": [
    {
      "name": "Hungry?",
      "id": 2,
      "yes": 10,
      "no": 1,
      "alive": 1,
      "democracy": 1
    }
  ]
}
 */
}
