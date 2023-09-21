from pathlib import Path
import json

decompiledCode = Path("C:/Users/Razer/IdeaProjects/course-02242-examples/decompiled")

classes = {}
for file in decompiledCode.glob("**/*.json"):
    with open(file) as p:
        doc = json.load(p)
        classes[doc["name"]] = doc

methods = {}
for cls in classes.values():
    for m in cls["methods"]:
        methods[(cls["name"], m["name"])] = m

def find_method(actualMethod):
    return methods[(actualMethod)]

def print_bytecode(actualMethods):
    method = find_method(actualMethods)
    assert method is not None
    print(method["code"]["bytecode"])

def bytecode_interpretor(am, log):
    memory = {}
    mstack = [([],[], (am, 0))]

    for i in range(0,10):
        log("->", mstack, end="")
        (lv, os, (am_, i)) = mstack[-1]
        lv = []
        b = find_method(am)["code"]["bytecode"][i]
        if b["opr"] == "return":
            if b["type"] == None:
                log(" (return)")
                return None
            elif b["type"] == "int":
                log(" (return)")
                return os[-1]
            else:
                log("usupported operation 1", b)
                return None
        elif b["opr"] == "push":
            log(" (push)")
            value = b["value"]
            if value["type"] == "integer":
                os.append(value["value"])
            else:
                log("unsupported operation 2", b)
                return None
            _ = mstack.pop()
            mstack.append((lv, os + [value["value"]], (am_, i + 1)))
        elif b["opr"] == "load":
            log(" (load)")
            # if index < len(lv):
            #     os.append(lv[index])
            if b["type"] == "int":
                lv.append(b["index"])
                os.append(lv)
            else:
                log("unsupported operation 3", b)
                return None
            _ = mstack.pop()
            mstack.append((lv, os, (am_, i + 1)))
        else:
            log("usupported operation", b)
            return None

cases = [
    ("dtu/compute/exec/Simple", "noop"),
    ("dtu/compute/exec/Simple", "zero"),
    ("dtu/compute/exec/Simple", "hundredAndTwo"),
    ("dtu/compute/exec/Simple", "identity"),
]

for case in cases:
    print("---", case, "----")
    s = bytecode_interpretor(case, print)
    print(s)
    print("--- done ----")
    print("")

