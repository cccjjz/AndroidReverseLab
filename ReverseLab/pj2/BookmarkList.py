import datetime
import json
from com.pnfsoftware.jeb.client.api import IScript, IconType, ButtonGroupType
from com.pnfsoftware.jeb.core import RuntimeProjectUtil
from BookmarkSet import BookmarkSet


class BookmarkList(IScript):

    def run(self, ctx):
        prj = ctx.getMainProject()
        assert prj, 'Need a project'

        bmstr = prj.getData(BookmarkSet.BMKEY)
        if not bmstr:
            ctx.displayMessageBox('Bookmarks', 'No recorded boolmarks yet!', IconType.INFORMATION, None)
            return

        bm = json.loads(bmstr)
        log('Current bookmarks (%d): %s' % (len(bm), bm))

        headers = ['Timestamp', 'Full Unit Path', 'Name', 'Fragment', 'Address', 'Comment']
        rows = []
        for uid, labelmap in bm.items():
            for label, addrMap in labelmap.items():
                for addr, e in addrMap.items():
                    unitPath, unitName, comment, ts = e
                    rows.append(
                        [datetime.datetime.fromtimestamp(ts).ctime(), unitPath, unitName, label, addr, comment, uid])

        index = ctx.displayList('Bookmarks', 'List of currently set bookmarks in the active project', headers, rows)
        if index < 0:
            return

        sel = rows[index]
        uid, label, addr = int(sel[6]), sel[3], sel[4]
        log('Selected: uid=%d,fragment=%s,addr=%s' % (uid, label, addr))

        unit = RuntimeProjectUtil.findUnitByUid(prj, uid)
        if not unit:
            print('Unit with uid=%d was not found in the project or no longer exists!' % uid)
            return

        if not ctx.openView(unit):
            print('Could not open view for unit!')
        else:
            f = ctx.findFragment(unit, label, True)
            if not f:
                print('Fragment "%s" not found!' % label)
            elif addr:
                f.setActiveAddress(addr)


def log(s):
    print(s)
