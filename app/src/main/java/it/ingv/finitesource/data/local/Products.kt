package it.ingv.finitesource.data.local

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import it.ingv.finitesource.R
import it.ingv.finitesource.ui.persistentbottomsheet.tabs.finitesource.FiniteSourceFragment
import it.ingv.finitesource.ui.persistentbottomsheet.tabs.footprints.FootprintsFragment
import it.ingv.finitesource.ui.persistentbottomsheet.tabs.scenarios.ScenariosFragment
import kotlinx.parcelize.Parcelize

@Parcelize
enum class Products(
	val productId: String,
	private val fragmentClass: () -> Fragment,
	val tabNameId: Int,
	val notAvailableTextId: Int
) : Parcelable {
	// the order of the tabs is the order of the enum
	FINITE_SOURCE(
		"finite_source",
		::FiniteSourceFragment,
		R.string.finite_source_tab_name,
		R.string.finite_source_not_available,
	),
	SCENARIOS(
		"scenarios",
		::ScenariosFragment,
		R.string.scenarios_tab_name,
		R.string.scenarios_not_available,
	),
	FOOTPRINTS(
		"footprints",
		::FootprintsFragment,
		R.string.footprints_tab_name,
		R.string.footprints_not_available,
	);

	/**
	 * Returns a new instance of the fragment connected to this product
	 */
	fun newFragmentInstance(): Fragment {
		val fragment = fragmentClass()
		val bundle = Bundle(1)
		bundle.putParcelable(PRODUCT_TAG, this)
		fragment.arguments = bundle
		return fragment
	}

	companion object {
		fun parseString(string: String): Products = try {
			entries.first { it.productId == string }
		} catch (e: NoSuchElementException) {
			throw IllegalArgumentException("No product with id $string")
		}
	}

	override fun toString(): String {
		return productId
	}
}

// this class represent a tab in the bottom sheet
// it is a fragment that is connected to a Product
/**
 * This class represent a tab in the bottom sheet.
 *
 * It is a fragment that is connected to a [Products].
 *
 * A tab extends this class and implements the logic to show the data of the product.
 */
abstract class ProductFragment : Fragment() {
	lateinit var product: Products

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// get the product from the arguments
		product = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			arguments?.getParcelable(PRODUCT_TAG, Products::class.java)
				?: throw Exception("Product not found")
		} else
			arguments?.getParcelable(PRODUCT_TAG) ?: throw Exception("Product not found")
	}
}

private const val PRODUCT_TAG = "product"


