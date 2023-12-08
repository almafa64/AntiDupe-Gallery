use proc_macro::TokenStream;
use quote::quote;
use syn::{parse_quote, ItemFn, LitStr, Meta, MetaList};

#[proc_macro_attribute]
pub fn java(attr: TokenStream, input: TokenStream) -> TokenStream {
    let attr: Meta = syn::parse(attr).unwrap();
    let metalist = attr.require_list().unwrap();
    match metalist.path.get_ident().unwrap().to_string().as_str() {
        "export" => java_export(metalist, input),
        _ => todo!(),
    }
}

fn java_export(metalist: &MetaList, input: TokenStream) -> TokenStream {
    let mut class_name = None;
    let mut rename = None;

    metalist
        .parse_nested_meta(|meta| {
            if meta.path.is_ident("class") {
                let class_name_in: String =
                    meta.value().unwrap().parse::<LitStr>().unwrap().value();
                class_name = Some(class_name_in);
            } else if meta.path.is_ident("rename") {
                let rename_in: String =
                    meta.value().unwrap().parse::<LitStr>().unwrap().value();
                rename = Some(rename_in);
            }
            Ok(())
        })
        .unwrap();

    let mut fn_decl: ItemFn = syn::parse(input).unwrap();

    let class_name = class_name.unwrap();
    let fn_name = if let Some(rename) = rename {
        rename
    } else {
        fn_decl.sig.ident.to_string()
    };

    let java_name = format!(
        "Java_{class_name}_{fn_name}",
        class_name = class_name.replace('.', "_")
    );

    fn_decl.attrs.push(parse_quote! {
        #[export_name = #java_name]
    });

    quote! { #fn_decl }.into()
}
