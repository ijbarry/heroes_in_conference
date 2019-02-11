using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NUnit.Framework;

[TestFixture]
public class TestClass {
    [TestCase]
    public void GetAllWonAchievementsTest() {
        Database db = new Database();
        db.AddAchievement(0, "", "");
        Assert.AreEqual(db.GetAllWonAchievements().Count, 0);
    }
}
