package com.baocode.cafe

/** Logical 2D tile map: the renderer is deliberately orthogonal, like a classic farming/life sim. */
enum class CellType5 { FLOOR, TABLE, CHAIR, COUNTER, KITCHEN, WALL, DOOR, PLANT }

data class CafeCell5(
    val col: Int,
    val row: Int,
    val type: CellType5 = CellType5.FLOOR,
    val passable: Boolean = type == CellType5.FLOOR || type == CellType5.DOOR,
    val height: Float = when (type) {
        CellType5.WALL -> 1.8f
        CellType5.COUNTER, CellType5.KITCHEN -> .75f
        CellType5.TABLE -> .55f
        CellType5.CHAIR -> .25f
        CellType5.PLANT -> .45f
        else -> 0f
    },
    val objectId: String? = null,
    val occupied: Boolean = false
)

data class CafeGrid5(val width: Int = 14, val height: Int = 10, val cells: List<CafeCell5>) {
    fun cell(c: Int, r: Int): CafeCell5? = cells.firstOrNull { it.col == c && it.row == r }
    fun isInside(c: Int, r: Int) = c in 0 until width && r in 0 until height
    fun isWalkable(c: Int, r: Int) = isInside(c, r) && cell(c, r)?.passable == true && cell(c, r)?.occupied != true
    fun withOccupied(c: Int, r: Int, value: Boolean): CafeGrid5 = copy(cells = cells.map { if (it.col == c && it.row == r) it.copy(occupied = value) else it })
}

data class GridPos5(val c: Int, val r: Int)

fun defaultCafeGrid5(): CafeGrid5 {
    val cells = MutableList(14 * 10) { i -> CafeCell5(i % 14, i / 14) }
    fun put(c: Int, r: Int, type: CellType5, objectId: String? = null) {
        if (c !in 0..13 || r !in 0..9) return
        cells[r * 14 + c] = CafeCell5(c, r, type, type == CellType5.FLOOR || type == CellType5.DOOR, objectId = objectId)
    }

    // Solid room shell, with a single entrance on the right wall.
    for (c in 0..13) { put(c, 0, CellType5.WALL, "wall_top"); put(c, 9, CellType5.WALL, "wall_bottom") }
    for (r in 1..8) { put(0, r, CellType5.WALL, "wall_left"); put(13, r, CellType5.WALL, "wall_right") }
    put(13, 5, CellType5.DOOR, "entrance")

    // Service area.
    put(2, 2, CellType5.KITCHEN, "kitchen")
    put(3, 2, CellType5.KITCHEN, "kitchen_2")
    put(4, 2, CellType5.COUNTER, "cashier")
    put(5, 2, CellType5.COUNTER, "counter_pastry")

    // Guest seating.
    put(8, 3, CellType5.TABLE, "table_window")
    put(10, 5, CellType5.TABLE, "table_guest")
    put(6, 7, CellType5.TABLE, "table_center")
    put(8, 4, CellType5.CHAIR, "chair_window")
    put(10, 6, CellType5.CHAIR, "chair_guest")
    put(6, 8, CellType5.CHAIR, "chair_center")

    // Greenery and small décor anchors.
    put(11, 2, CellType5.PLANT, "plant_window")
    put(2, 6, CellType5.PLANT, "plant_left")
    put(12, 7, CellType5.PLANT, "plant_door")
    return CafeGrid5(cells = cells)
}

/** Shortest orthogonal tile path. Diagonals are intentionally disabled for readable grid movement. */
fun findCafePath5(grid: CafeGrid5, fromC: Int, fromR: Int, toC: Int, toR: Int): List<GridPos5> {
    if (!grid.isWalkable(toC, toR) && !(fromC == toC && fromR == toR)) return emptyList()
    data class Node(val c: Int, val r: Int)
    val start = Node(fromC, fromR)
    val goal = Node(toC, toR)
    val queue = ArrayDeque<Node>()
    val parent = mutableMapOf<Node, Node?>()
    queue.add(start); parent[start] = null
    val dirs = arrayOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
    while (queue.isNotEmpty()) {
        val n = queue.removeFirst()
        if (n == goal) break
        for ((dc, dr) in dirs) {
            val next = Node(n.c + dc, n.r + dr)
            if (next !in parent && (grid.isWalkable(next.c, next.r) || next == goal)) {
                parent[next] = n
                queue.addLast(next)
            }
        }
    }
    if (goal !in parent) return emptyList()
    val path = mutableListOf<GridPos5>(); var cur: Node? = goal
    while (cur != null) { path += GridPos5(cur.c, cur.r); cur = parent[cur] }
    return path.asReversed().drop(1)
}
